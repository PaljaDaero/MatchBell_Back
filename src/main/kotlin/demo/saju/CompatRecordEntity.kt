package demo.saju

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "compat_records",
    indexes = [
        Index(name = "idx_compat_user_pair", columnList = "user_a_id,user_b_id"),
        Index(name = "idx_compat_final_score", columnList = "final_score DESC")
    ]
)
class CompatRecordEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_a_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_compat_user_a")
    )
    var userA: UserEntity,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_b_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_compat_user_b")
    )
    var userB: UserEntity,

    @Column(name = "final_score", nullable = false)
    var finalScore: Double,

    @Column(name = "stress_score", nullable = false)
    var stressScore: Double,

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    // JPA 용 기본 생성자
    protected constructor() : this(
        userA = UserEntity(),
        userB = UserEntity(),
        finalScore = 0.0,
        stressScore = 0.0,
        createdAt = LocalDateTime.now()
    )
}
