package demo.matching

import demo.auth.JwtTokenProvider
import demo.profile.ProfileRepository
import demo.profile.ProfileUnlockService
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
    private val compatController: CompatController,
    private val profileUnlockService: ProfileUnlockService   // 🔹 新增：主页解锁服务
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
     * 매칭된 상대방 프로필 조회
     * GET /me/matches/{targetUserId}/profile
     * - 상대방이 나와 매칭된 상태여야 함
     * - 내가 상대방의 프로필을 잠금 해제했어야 함
     * - 궁합 정보 포함
     */
    @GetMapping("/matches/{targetUserId}/profile")
    fun getMatchedProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable targetUserId: Long
    ): MatchProfileResponse {
        val meUserId = extractUserIdFromHeader(authHeader)

        // 1) 매칭 상태 확인
        matchingService.checkHasMatch(meUserId, targetUserId)

        // 2) 프로필 잠금 해제 상태 확인
        if (!profileUnlockService.isUnlocked(meUserId, targetUserId)) {
            throw ResponseStatusException(
                HttpStatus.PAYMENT_REQUIRED,
                "프로필 잠금 해제가 필요합니다."
            )
        }

        // 3) 상대방 프로필 조회
        val profile = profileRepository.findByUserId(targetUserId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "사용자를 찾을 수 없습니다."
            )

        // 4) 궁합 정보 조회
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
