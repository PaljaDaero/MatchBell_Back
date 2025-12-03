package demo.profile

import demo.cookie.CookieService
import demo.matching.MatchRepository
import demo.matching.MatchStatus
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class ProfileUnlockService(
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository,
    private val profileUnlockRepository: ProfileUnlockRepository,
    private val cookieService: CookieService
) {

    companion object {
        // 프로필 해제 비용
        private const val PROFILE_UNLOCK_COST: Long = 10L
    }

    /**
     * 프로필 해제 여부 확인
     */
    @Transactional(readOnly = true)
    fun isUnlocked(viewerId: Long, targetId: Long): Boolean {
        return profileUnlockRepository.existsByViewerIdAndTargetId(viewerId, targetId)
    }

    /**
     *  프로필 해제
     * @param viewerId 해제 시도 사용자 ID
     * @param targetId 해제 대상 사용자 ID
     * @return 해제 결과
     * @throws ResponseStatusException
     *   - 400 BAD_REQUEST: 자신을 해제하려는 경우
     *  - 404 NOT_FOUND: viewerId 또는 targetId에 해당하는 사용자가 없는 경우
     * - 403 FORBIDDEN: 매칭이 없거나 비활성 상태인 경우
     */
    @Transactional
    fun unlockProfile(viewerId: Long, targetId: Long): ProfileUnlockResult {
        if (viewerId == targetId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "자신의 프로필은 해제할 수 없습니다."
            )
        }

        val viewer = userRepository.findById(viewerId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }
        val target = userRepository.findById(targetId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }

        // 1)  매칭 확인
        val (u1, u2) =
            if ((viewer.id ?: 0L) <= (target.id ?: 0L)) viewer to target else target to viewer

        val match = matchRepository.findByUser1AndUser2(u1, u2)
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "매칭이 존재하지 않습니다."
            )

        if (match.status != MatchStatus.ACTIVE) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "매칭이 활성 상태가 아닙니다."
            )
        }

        // 2)  이미 해제되었는지 확인
        if (isUnlocked(viewerId, targetId)) {
            return ProfileUnlockResult(
                unlocked = true,
                alreadyUnlocked = true,
                cost = 0L,
                balanceAfter = null
            )
        }

        // 3)  코스트 차감
        val wallet = cookieService.spend(
            userId = viewerId,
            amount = PROFILE_UNLOCK_COST,
            reason = "PROFILE_UNLOCK:$targetId"
        )

        //  4)  해제 기록 저장
        profileUnlockRepository.save(
            ProfileUnlockEntity(
                viewer = viewer,
                target = target
            )
        )

        return ProfileUnlockResult(
            unlocked = true,
            alreadyUnlocked = false,
            cost = PROFILE_UNLOCK_COST,
            balanceAfter = wallet.balance
        )
    }
}
