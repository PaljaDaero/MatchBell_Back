package demo.chat

import demo.matching.MatchEntity
import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "messages",
    indexes = [
        Index(name = "idx_message_match", columnList = "match_id, sent_at")
    ]
)
data class MessageEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "match_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_message_match")
    )
    val match: MatchEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "sender_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_message_sender")
    )
    val sender: UserEntity,

    @Column(nullable = false, length = 2000)
    val content: String,

    @Column(name = "sent_at", nullable = false)
    val sentAt: LocalDateTime = LocalDateTime.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus {
    SENT, READ, DELETED
}
