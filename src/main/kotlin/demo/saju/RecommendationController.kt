package demo.saju

import demo.auth.JwtTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/recommendations")
class RecommendationController(
    private val recommendationService: RecommendationService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * 추천 사용자 리스트 조회
     *
     * GET /recommendations?limit=20
     * Authorization: Bearer <JWT>
     */
    @GetMapping
    fun getRecommendations(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestParam("limit", required = false, defaultValue = "20") limit: Int
    ): RecommendationListResponse {
        val userId = extractUserIdFromHeader(authHeader)
        return recommendationService.getRecommendations(userId, limit)
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
