package demo.config

import demo.auth.JwtTokenProvider
import demo.chat.UserPrincipal
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val jwtTokenProvider: JwtTokenProvider
) : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // 웹소켓 엔드포인트 등록 ws://<server>/ws 或 wss://<server>/ws
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 메시지 브로커 설정 convertAndSend("/topic/chat.{matchId}", ...)
        registry.enableSimpleBroker("/topic")
        // 클라이언트에서 send("/app/xxx", ...) 로 메시지 보낼 때 매핑
        registry.setApplicationDestinationPrefixes("/app")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(object : ChannelInterceptor {
            override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
                val accessor = MessageHeaderAccessor
                    .getAccessor(message, StompHeaderAccessor::class.java)
                    ?: return message

                // STOMP CONNECT 프레임일 때 JWT 인증 처리
                if (accessor.command == StompCommand.CONNECT) {
                    // STOMP native header 에서 Authorization 꺼내기
                    val rawAuth =
                        accessor.getFirstNativeHeader("Authorization")
                            ?: accessor.getFirstNativeHeader("authorization")

                    val token = rawAuth
                        ?.removePrefix("Bearer ")
                        ?.trim()

                    if (!token.isNullOrBlank()) {
                        val userId = jwtTokenProvider.parseUserId(token)

                        if (userId != null) {
                            // UserPrincipal 생성해서 STOMP 세션에 user 로 설정
                            val userPrincipal = UserPrincipal(userId)
                            accessor.user = userPrincipal

                            println(">>> [WS-AUTH] CONNECT success: userId=$userId")
                        } else {
                            println(">>> [WS-AUTH] invalid token on CONNECT: $rawAuth")
                        }
                    } else {
                        println(">>> [WS-AUTH] no Authorization header on CONNECT")
                    }
                }

                return message
            }
        })
    }
}
