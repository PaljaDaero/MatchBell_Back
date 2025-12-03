package demo.matching

import demo.auth.JwtTokenProvider
import demo.profile.ProfileRepository
import demo.profile.ProfileUnlockService
import demo.profile.ProfileViewController
import demo.profile.ProfileViewResponse
import demo.saju.CompatController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/me")
class MatchingController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val matchingService: MatchingService,
    private val profileRepository: ProfileRepository,
    private val compatController: CompatController,
    private val profileUnlockService: ProfileUnlockService,
    private val profileViewController: ProfileViewController   // ✅ 复用 ProfileViewController 逻辑
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
     * 궁금해요 보낸 목록
     * GET /me/curious/sent
     */
    @GetMapping("/curious/sent")
    fun getSentCurious(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<CuriousUserSummary> {
        val meUserId = extractUserIdFromHeader(authHeader)
        return matchingService.getSentCurious(meUserId)
    }

    /**
     * 궁금해요 받은 목록
     * GET /me/curious/received
     */
    @GetMapping("/curious/received")
    fun getReceivedCurious(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<CuriousUserSummary> {
        val meUserId = extractUserIdFromHeader(authHeader)
        return matchingService.getReceivedCurious(meUserId)
    }

    /**
     * 매칭된 목록
     * GET /me/matches
     */
    @GetMapping("/matches")
    fun getMatches(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<MatchSummary> {
        val meUserId = extractUserIdFromHeader(authHeader)
        return matchingService.getMatches(meUserId)
    }

    /**
     * 프로필 잠금 해제 요청
     * POST /me/matches/{targetUserId}/profile/unlock
     * - 쿠키 차감 후 잠금 해제
     * - 단방향 해제 (상대방이 내 프로필을 보기 위해서는 상대방이 별도 해제 필요)
     */
    @PostMapping("/matches/{targetUserId}/profile/unlock")
    fun unlockMatchedProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable targetUserId: Long
    ): ProfileUnlockResponse {
        val meUserId = extractUserIdFromHeader(authHeader)

        val result = profileUnlockService.unlockProfile(meUserId, targetUserId)

        return ProfileUnlockResponse(
            unlocked = result.unlocked,
            alreadyUnlocked = result.alreadyUnlocked,
            cost = result.cost,
            balanceAfter = result.balanceAfter
        )
    }

    /**
     * 매칭된 상대방 프로필 조회 (통합 버전)
     *
     * GET /me/matches/{targetUserId}/profile
     *
     * - 응답 스키마는 /profiles/{targetUserId} 와 동일하게 ProfileViewResponse 사용
     * - 하지만 /me/matches/... 경로에서는 "반드시 매칭 상태" 를 강제:
     *   - 매칭이 아니면 403 FORBIDDEN
     * - 매칭 + 잠금 여부에 따라 detail, canChat, canUnlock 등의 필드는
     *   ProfileViewController.viewProfile() 의 로직을 그대로 따름
     */
    @GetMapping("/matches/{targetUserId}/profile")
    fun getMatchedProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable targetUserId: Long
    ): ProfileViewResponse {
        // 1) 먼저 공통 로직(/profiles/{targetUserId}) 호출해서 상태를 계산
        val baseResponse = profileViewController.viewProfile(authHeader, targetUserId)

        // 2) /me/matches/... 경로에서는 "매칭된 사용자만" 허용
        //    - 자기 자신은 허용 (isSelf == true)
        //    - 그 외 isMatched == false 이면 403
        if (!baseResponse.isSelf && !baseResponse.isMatched) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "매칭된 사용자만 이 경로로 프로필을 조회할 수 있습니다."
            )
        }

        // 3) 그대로 반환 (스키마는 /profiles 와 100% 동일)
        return baseResponse
    }

    /**
     * Authorization 헤더에서 JWT 추출 후 userId 파싱
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
