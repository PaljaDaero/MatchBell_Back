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
        private const val PROFILE_UNLOCK_COST: Long = 10L
    }

    @Transactional(readOnly = true)
    fun isUnlocked(viewerId: Long, targetId: Long): Boolean {
        return profileUnlockRepository.existsByViewerIdAndTargetId(viewerId, targetId)
    }

   
    @Transactional
    fun unlockProfile(viewerId: Long, targetId: Long): ProfileUnlockResult {
        if (viewerId == targetId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "자기의 프로필은 잠금 해제할 수 없습니다."
            )
        }

        val viewer = userRepository.findById(viewerId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다")
        }
        val target = userRepository.findById(targetId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다")
        }

        // 1) 매칭 여부 확인 (err 403)
        val (u1, u2) =
            if ((viewer.id ?: 0L) <= (target.id ?: 0L)) viewer to target else target to viewer

        val match = matchRepository.findByUser1AndUser2(u1, u2)
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                " 매칭된 사용자만 프로필 잠금 해제를 할 수 있습니다."
            )

        if (match.status != MatchStatus.ACTIVE) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "매칭된 사용자만 프로필 잠금 해제를 할 수 있습니다."
            )
        }

        // 2) 이미 잠금 해제된 경우 바로 반환
        if (isUnlocked(viewerId, targetId)) {
            return ProfileUnlockResult(
                unlocked = true,
                alreadyUnlocked = true,
                cost = 0L,
                balanceAfter = null
            )
        }

        // 3) 쿠키 차감
        val wallet = cookieService.spend(
            userId = viewerId,
            amount = PROFILE_UNLOCK_COST,
            reason = "PROFILE_UNLOCK:$targetId"
        )

        // 4) 잠금 해제 기록 생성
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
