package demo.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig : WebSocketMessageBrokerConfigurer {

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        // 웹소켓 엔드포인트 등록 ws://<server>/ws 或 wss://<server>/ws
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")   
            .withSockJS()                   
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // 메시지 브로커 설정 convertAndSend("/topic/chat.{matchId}", ...)
        registry.enableSimpleBroker("/topic")

        // 클라이언트에서 send("/app/hello", ...) 로 메시지 보낼 때 매핑
        registry.setApplicationDestinationPrefixes("/app")
    }
}
