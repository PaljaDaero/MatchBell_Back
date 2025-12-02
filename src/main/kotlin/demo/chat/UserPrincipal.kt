package demo.chat

import java.security.Principal

/**
 * STOMP WebSocket 세션에 매달릴 사용자 정보.
 * 지금은 단순히 userId 만 넣고, name = userId.toString() 으로 사용.
 */
data class UserPrincipal(
    val userId: Long
) : Principal {
    override fun getName(): String = userId.toString()
}
