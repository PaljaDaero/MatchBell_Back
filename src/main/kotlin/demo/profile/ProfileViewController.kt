package demo.profile

import demo.auth.JwtTokenProvider
import demo.matching.MatchRepository
import demo.matching.MatchStatus
import demo.saju.CompatController
import demo.saju.CompatRequest
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.Period

@RestController
@RequestMapping("/profiles")
class ProfileViewController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository,
    private val profileUnlockService: ProfileUnlockService,
    private val compatController: CompatController
) {

    /**
        * 查看用户主页
    * GET /profiles/{targetUserId}
     */
    @GetMapping("/{targetUserId}")
    fun viewProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable targetUserId: Long
    ): ProfileViewResponse {
        val meUserId = extractUserIdFromHeader(authHeader)

        val profile = profileRepository.findByUserId(targetUserId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "프로필을 찾을 수 없습니다."
            )

        val age = calculateAge(profile.birthDate)

        val basic = BasicProfileInfo(
            userId = targetUserId,
            nickname = profile.nickname,
            age = age,
            region = profile.region,
            avatarUrl = profile.avatarUrl,
            shortIntro = profile.intro?.take(40), 
            tendency = profile.tendency
        )

        // 1. 자신의 프로필 
        if (meUserId == targetUserId) {
            val detail = DetailProfileInfo(
                gender = profile.gender,
                birth = profile.birthDate,
                job = profile.job,
                intro = profile.intro,
                compat = null      
            )
            return ProfileViewResponse(
                basic = basic,
                detail = detail,
                isSelf = true,
                isMatched = false,
                hasUnlocked = true,
                canChat = false,
                canUnlock = false
            )
        }

        // 2. 다른 사람의 프로필 → 매칭 여부 확인
        val me = userRepository.findById(meUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다.")
        }
        val target = userRepository.findById(targetUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다.")
        }

        val (u1, u2) =
            if ((me.id ?: 0L) <= (target.id ?: 0L)) me to target else target to me

        val match = matchRepository.findByUser1AndUser2(u1, u2)
        val isMatched = (match != null && match.status == MatchStatus.ACTIVE)

        // 매칭이 안 된 경우 → basic 만 반환
        if (!isMatched) {
            return ProfileViewResponse(
                basic = basic,
                detail = null,
                isSelf = false,
                isMatched = false,
                hasUnlocked = false,
                canChat = false,
                canUnlock = false
            )
        }

        // 3. 매칭 + 잠금 해제 여부 확인
        val hasUnlocked = profileUnlockService.isUnlocked(meUserId, targetUserId)

        if (!hasUnlocked) {
            // 매칭됨 + 잠금 해제 안 됨 → detail 없음
            return ProfileViewResponse(
                basic = basic,
                detail = null,
                isSelf = false,
                isMatched = true,
                hasUnlocked = false,
                canChat = true,
                canUnlock = true
            )
        }

        // 4. 매칭 + 잠금 해제 됨 → detail 반환
        val compat = compatController.getCompatScore(
            CompatRequest(
                meUserId = meUserId,
                targetUserId = targetUserId
            )
        )

        val detail = DetailProfileInfo(
            gender = profile.gender,
            birth = profile.birthDate,
            job = profile.job,
            intro = profile.intro,
            compat = compat
        )

        return ProfileViewResponse(
            basic = basic,
            detail = detail,
            isSelf = false,
            isMatched = true,
            hasUnlocked = true,
            canChat = true,
            canUnlock = false
        )
    }

    private fun calculateAge(birth: LocalDate): Int =
        Period.between(birth, LocalDate.now()).years

    private fun extractUserIdFromHeader(authHeader: String?): Long {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "인증 정보가 없습니다."
            )
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        return jwtTokenProvider.parseUserId(token)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "유효하지 않은 토큰입니다."
            )
    }
}
