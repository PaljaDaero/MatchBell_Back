package demo.profile

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDate

@Entity
@Table(name = "profiles")
data class ProfileEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_profile_user")
    )
    val user: UserEntity,

    @Column(nullable = false, length = 30)
    val nickname: String,

    @Column(name = "intro", length = 500)
    val intro: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val gender: Gender,

    @Column(name = "birth_date", nullable = false)
    val birthDate: LocalDate,

    @Column(name = "region", length = 100)
    val region: String? = null,

    @Column(name = "job", length = 100)
    val job: String? = null,

    @Column(name = "avatar_url", length = 500)
    val avatarUrl: String? = null,

    @Column(name = "tendency", length = 1000)
    val tendency: String? = null
) {

    /**
     * JPA 용 기본 생성자
     */
    protected constructor() : this(
        id = 0L,
        user = UserEntity(),           // 임시 객체
        nickname = "",
        intro = null,
        gender = Gender.OTHER,
        birthDate = LocalDate.now(),
        region = null,
        job = null,
        avatarUrl = null,
        tendency = null
    )
}

enum class Gender {
    MALE, FEMALE, OTHER
}
