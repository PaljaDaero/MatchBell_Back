package demo.saju

import demo.profile.Gender
import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "my_compat_records",
    indexes = [
        Index(name = "idx_my_compat_user", columnList = "user_id"),
        Index(name = "idx_my_compat_created", columnList = "created_at")
    ]
)
class MyCompatRecordEntity(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_my_compat_user")
    )
    var user: UserEntity,

    @Column(name = "target_name", nullable = false, length = 50)
    var targetName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "target_gender", nullable = false, length = 10)
    var targetGender: Gender,

    @Column(name = "target_birth", nullable = false)
    var targetBirth: LocalDate,

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
        user = UserEntity(),
        targetName = "",
        targetGender = Gender.OTHER,
        targetBirth = LocalDate.now(),
        finalScore = 0.0,
        stressScore = 0.0,
        createdAt = LocalDateTime.now()
    )
}
