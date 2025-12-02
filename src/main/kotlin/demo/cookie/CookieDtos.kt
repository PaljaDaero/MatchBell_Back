package demo.cookie

import java.time.LocalDateTime

data class CookieBalanceResponse(
    val balance: Long
)

data class CookieChangeRequest(
    val amount: Long,
    val reason: String? = null
)

data class CookieTransactionResponse(
    val type: String,
    val amount: Long,
    val balanceAfter: Long,
    val reason: String?,
    val createdAt: LocalDateTime
)
