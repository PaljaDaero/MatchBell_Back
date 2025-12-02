package demo.user

import jakarta.persistence.*

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uk_user_email", columnNames = ["email"])
    ]
)
class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null // 신규 생성 시 null

    @Column(nullable = false, length = 100)
    var email: String = ""

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = ""

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: UserStatus = UserStatus.ACTIVE

    @Column(name = "cookie", nullable = false)
    var cookie: Long = 0L

}

enum class UserStatus {
    ACTIVE,   // 활성
    BLOCKED,  // 차단
    DELETED   // 삭제
}
