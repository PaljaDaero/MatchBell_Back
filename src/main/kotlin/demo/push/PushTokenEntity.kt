package demo.push

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "push_tokens",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_push_token_value",
            columnNames = ["token"]
        )
    ]
)
data class PushTokenEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_pushtoken_user")
    )
    val user: UserEntity,

    @Column(nullable = false, length = 1024)
    val token: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "platform", nullable = false, length = 20)
    val platform: PushPlatform = PushPlatform.ANDROID,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "last_used_at")
    val lastUsedAt: LocalDateTime? = null,

    @Column(name = "is_active", nullable = false)
    val isActive: Boolean = true
)

enum class PushPlatform {
    ANDROID, IOS, WEB
}
