package demo.chat

import java.time.LocalDateTime

// WebSocket /app/chat.send 
data class ChatSendRequest(
    val matchId: Long,
    val content: String
)

// 메시지
data class ChatMessageResponse(
    val id: Long,
    val matchId: Long,
    val senderId: Long,
    val content: String,
    val sentAt: LocalDateTime,
    val status: MessageStatus
)

// 채팅방 목록 아이템
data class ChatRoomItemResponse(
    val matchId: Long,
    val otherUserId: Long,
    val otherNickname: String,
    val otherAvatarUrl: String?,
    val lastMessage: String?,
    val lastMessageTime: LocalDateTime?,
    val unreadCount: Long 
)
