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

        // --- 공통 프로필 데이터: 어떤 상황에서도 그대로 내려감 ---
        var basic = BasicProfileInfo(
            userId = targetUserId,
            nickname = profile.nickname,
            age = age,
            region = profile.region,
            avatarUrl = profile.avatarUrl,
            shortIntro = profile.intro?.take(40),
            tendency = profile.tendency,
            gender = profile.gender,
            birth = profile.birthDate,
            job = profile.job,
            intro = profile.intro,
            compat = null          // 기본은 null, 필요하면 아래에서 채움
        )

        // 1. 내 프로필
        if (meUserId == targetUserId) {
            // 내 프로필은 잠금 개념 없다고 보고 hasUnlocked = true 고정
            return ProfileViewResponse(
                basic = basic,
                isSelf = true,
                isMatched = false,
                hasUnlocked = true,
                canChat = false,
                canUnlock = false
            )
        }

        // 2. 다른 유저: 매칭 여부 확인
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

        // 매칭 여부랑 상관없이 basic 은 그대로 내려감 (여기가 핵심!)
        var hasUnlocked = false
        var canChat = false
        var canUnlock = false

        if (isMatched) {
            canChat = true

            // 백엔드는 그냥 “현재 해제 여부”만 알려줌 (UI는 프론트가 조절)
            hasUnlocked = profileUnlockService.isUnlocked(meUserId, targetUserId)
            canUnlock = !hasUnlocked

            // 궁합은 매칭 + 해제된 상태에서만 계산하고 basic.compat 에 넣기
            if (hasUnlocked) {
                val compat = compatController.getCompatScore(
                    CompatRequest(
                        meUserId = meUserId,
                        targetUserId = targetUserId
                    )
                )
                basic = basic.copy(compat = compat)
            }
        }

        return ProfileViewResponse(
            basic = basic,
            isSelf = false,
            isMatched = isMatched,
            hasUnlocked = hasUnlocked,
            canChat = canChat,
            canUnlock = canUnlock
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
