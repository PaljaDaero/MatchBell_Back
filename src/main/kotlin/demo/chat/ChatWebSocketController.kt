package demo.chat

import demo.chat.MessageEntity
import demo.chat.MessageRepository
import demo.chat.ChatMessageResponse
import demo.chat.ChatSendRequest
import demo.chat.UserPrincipal
import demo.matching.MatchRepository
import demo.matching.MatchStatus
import demo.user.UserRepository
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.transaction.annotation.Transactional

@Controller
class ChatWebSocketController(
    private val matchRepository: MatchRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @MessageMapping("/chat.send")
    @Transactional
    fun handleSendMessage(
        @Payload req: ChatSendRequest,
        headers: SimpMessageHeaderAccessor
    ) {
        val principal = headers.user as? UserPrincipal
            ?: throw IllegalStateException("No authenticated user")

        val senderId = principal.userId

        // 1) match 존재 여부 + sender 가 match 의 당사자인지 확인
        val match = matchRepository.findById(req.matchId).orElseThrow()

        val isParticipant =
            (match.user1.id == senderId) || (match.user2.id == senderId)
        if (!isParticipant) {
            return
        }

        // 🔒 차단/종료된 매칭이면 더 이상 메세지 전송 불가
        if (match.status != MatchStatus.ACTIVE) {
            return
        }

        val sender = userRepository.findById(senderId).orElseThrow()


        // 2) 메세지 저장
        val saved = messageRepository.save(
            MessageEntity(
                match = match,
                sender = sender,
                content = req.content
            )
        )

        val resp = ChatMessageResponse(
            id = saved.id,
            matchId = match.id,
            senderId = senderId,
            content = saved.content,
            sentAt = saved.sentAt,
            status = saved.status
        )

        // 3) 메세지 구독자들에게 전송 /topic/chat.{matchId}
        messagingTemplate.convertAndSend(
            "/topic/chat.${match.id}",
            resp
        )
    }
}
