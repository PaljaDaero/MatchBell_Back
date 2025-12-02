package demo.matching

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "matches",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_match_pair",
            columnNames = ["user1_id", "user2_id"]
        )
    ]
)
data class MatchEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    // 약속: 항상 user1.id < user2.id 로 저장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user1_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_match_user1")
    )
    val user1: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user2_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_match_user2")
    )
    val user2: UserEntity,

    @Column(name = "thread_id", length = 100)
    val threadId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val status: MatchStatus = MatchStatus.ACTIVE,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class MatchStatus {
    ACTIVE,
    BLOCKED,
    ENDED
}
