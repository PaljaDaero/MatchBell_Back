package demo.saju

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalTime

@Entity
@Table(name = "saju")
data class SajuEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_saju_user")
    )
    val user: UserEntity,

    @Column(name = "birth_date", nullable = false)
    val birthDate: LocalDate,

    @Column(name = "birth_time")
    val birthTime: LocalTime? = null,

    @Column(name = "is_lunar", nullable = false)
    val isLunar: Boolean = false,

    @Column(name = "raw_payload", columnDefinition = "text")
    val rawPayload: String? = null
)
