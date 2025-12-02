package demo.cookie

import demo.auth.JwtTokenProvider
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/me/cookie")
class CookieController(
    private val cookieService: CookieService,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * GET /me/cookie
     * 쿠키 잔액 조회
     */
    @GetMapping
    fun getMyBalance(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): CookieBalanceResponse {
        val userId = extractUserIdFromHeader(authHeader)
        val balance = cookieService.getBalance(userId)
        return CookieBalanceResponse(balance = balance)
    }

    /**
     * POST /me/cookie/earn
     * 쿠키 충전/획득（예: 활동 보상, 출석 체크 등）
     */
    @PostMapping("/earn")
    fun earnCookie(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody req: CookieChangeRequest
    ): CookieBalanceResponse {
        val userId = extractUserIdFromHeader(authHeader)
        val wallet = cookieService.earn(userId, req.amount, req.reason)
        return CookieBalanceResponse(balance = wallet.balance)
    }

    /**
     * POST /me/cookie/spend
        * 쿠키 소비（예: 아이템 구매 등）/profiles/{id}/unlock
     */
    @PostMapping("/spend")
    fun spendCookie(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody req: CookieChangeRequest
    ): CookieBalanceResponse {
        val userId = extractUserIdFromHeader(authHeader)
        val wallet = cookieService.spend(userId, req.amount, req.reason)
        return CookieBalanceResponse(balance = wallet.balance)
    }

    /**
     * GET /me/cookie/history
        * 내 쿠키 거래 내역 조회
     */
    @GetMapping("/history")
    fun getMyHistory(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<CookieTransactionResponse> {
        val userId = extractUserIdFromHeader(authHeader)
        val txs = cookieService.getRecentTransactions(userId)
        return txs.map {
            CookieTransactionResponse(
                type = it.type.name,
                amount = it.amount,
                balanceAfter = it.balanceAfter,
                reason = it.reason,
                createdAt = it.createdAt
            )
        }
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
