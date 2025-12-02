package demo.matching

import demo.auth.JwtTokenProvider
import demo.profile.ProfileRepository
import demo.saju.CompatController
import demo.saju.CompatRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/me")
class MatchingController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val matchingService: MatchingService,
    private val profileRepository: ProfileRepository,
    private val compatController: CompatController
) {

    /**
     * 궁금해요 보내기
     * POST /me/curious/{targetUserId}
     */
    @PostMapping("/curious/{targetUserId}")
    fun sendCurious(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable targetUserId: Long
    ): CuriousResponse {
        val meUserId = extractUserIdFromHeader(authHeader)
        return matchingService.sendCurious(meUserId, targetUserId)
    }

    /**
     * 내가 보낸 궁금해요 리스트
     */
    @GetMapping("/curious/sent")
    fun getSentCurious(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<CuriousUserSummary> {
        val meUserId = extractUserIdFromHeader(authHeader)
        return matchingService.getSentCurious(meUserId)
    }

    /**
     * 내가 받은 궁금해요 리스트
     */
    @GetMapping("/curious/received")
    fun getReceivedCurious(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<CuriousUserSummary> {
        val meUserId = extractUserIdFromHeader(authHeader)
        return matchingService.getReceivedCurious(meUserId)
    }

    /**
     * 매칭 리스트
     */
    @GetMapping("/matches")
    fun getMatches(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<MatchSummary> {
        val meUserId = extractUserIdFromHeader(authHeader)
        return matchingService.getMatches(meUserId)
    }

    /**
     * 매칭된 상대 프로필 + 궁합 결과
     * GET /me/matches/{targetUserId}/profile
     */
    @GetMapping("/matches/{targetUserId}/profile")
    fun getMatchedProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable targetUserId: Long
    ): MatchProfileResponse {
        val meUserId = extractUserIdFromHeader(authHeader)

        // 매칭 관계 확인 (없으면 403)
        matchingService.checkHasMatch(meUserId, targetUserId)

        val profile = profileRepository.findByUserId(targetUserId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "상대 프로필을 찾을 수 없습니다."
            )

        // 기존 /compat/score 로직 재사용
        val compat = compatController.getCompatScore(
            CompatRequest(
                meUserId = meUserId,
                targetUserId = targetUserId
            )
        )

        return MatchProfileResponse(
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
