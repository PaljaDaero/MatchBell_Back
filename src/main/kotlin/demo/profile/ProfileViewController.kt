package demo.profile

import demo.auth.JwtTokenProvider
import demo.saju.CompatController
import demo.saju.CompatRequest
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@RestController
@RequestMapping("/profiles")
class ProfileViewController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val compatController: CompatController
) {

    /**
     * 프로필 상세 조회 (잠금/해제 로직 완전히 제거, 모든 유저 쌍에 대해 궁합 계산)
     *
     * GET /profiles/{targetUserId}
     *
     * - 헤더: Authorization: Bearer <JWT>
     * - 응답: ProfileViewResponse (플랫 구조)
     *
     * 동작:
     *  - 항상 targetUserId 의 프로필 정보를 그대로 내려줌
     *  - meUserId != targetUserId 인 경우:
     *      → meUserId 와 targetUserId 의 궁합 점수 계산해서 compat 에 넣어줌
     *  - meUserId == targetUserId 인 경우:
     *      → compat = null (원하면 자기자신 기준으로도 계산하도록 바꿀 수 있음)
     *  - “해제/잠금 여부” 는 서버에서 일절 관리하지 않음.
     *    프론트가 이 데이터를 바탕으로 자체 unlock 로직으로 필드 숨김/blur 처리.
     */
    @GetMapping("/{targetUserId}")
    fun viewProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable targetUserId: Long
    ): ProfileViewResponse {
        val meUserId = extractUserIdFromHeader(authHeader)

        // 프로필 존재 확인
        val profile = profileRepository.findByUserId(targetUserId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "프로필을 찾을 수 없습니다."
            )

        // (선택) 유저 존재 확인 – user 테이블에 꼭 있어야 한다면 유지
        userRepository.findById(meUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 찾을 수 없습니다.")
        }
        userRepository.findById(targetUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "상대 사용자 정보를 찾을 수 없습니다.")
        }

        // 🔹 궁합 점수 계산: "모든 유저 쌍"에 대해 계산 (자기 자신만 예외로 둘지 여부는 정책)
        val compat = if (meUserId != targetUserId) {
            compatController.getCompatScore(
                CompatRequest(
                    meUserId = meUserId,
                    targetUserId = targetUserId
                )
            )
        } else {
            null   // 자기 자신 프로필에서는 굳이 궁합을 계산하지 않음
        }

        return ProfileViewResponse(
            userId = targetUserId,
            nickname = profile.nickname,
            intro = profile.intro,
            gender = profile.gender,
            birth = profile.birthDate,
            region = profile.region,
            job = profile.job,
            avatarUrl = profile.avatarUrl,
            tendency = profile.tendency,
            compat = compat
        )
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출 후 userId 파싱
     */
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
