package demo.cookie

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cookie_wallets")
class CookieWalletEntity() {  // JPA 용 기본 생성자

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null       // 보통 JPA에서는 nullable 로 둠

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_cookie_user")
    )
    lateinit var user: UserEntity   // 늦게 채우는 필드

    @Column(name = "balance", nullable = false)
    var balance: Long = 0L

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    // 간단한 동시성 제어용 optimistic lock
    @Version
    var version: Long? = null

    // 편의용 생성자 (서비스 코드에서 사용할 것)
    constructor(
        user: UserEntity,
        balance: Long = 0L,
        updatedAt: LocalDateTime = LocalDateTime.now()
    ) : this() {
        this.user = user
        this.balance = balance
        this.updatedAt = updatedAt
    }
}
