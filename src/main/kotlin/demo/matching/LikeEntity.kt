package demo.matching

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "likes",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_like_from_to",
            columnNames = ["from_user_id", "to_user_id"]
        )
    ],
    indexes = [
        Index(name = "idx_like_to_user", columnList = "to_user_id")
    ]
)
data class LikeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "from_user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_like_from_user")
    )
    val fromUser: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "to_user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_like_to_user")
    )
    val toUser: UserEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: LikeStatus = LikeStatus.ACTIVE,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class LikeStatus {
    ACTIVE,
    CANCELLED,
    EXPIRED
}
