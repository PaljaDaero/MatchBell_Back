package demo.cookie

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cookie_wallets")
data class CookieWalletEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_cookie_user")
    )
    val user: UserEntity,

    @Column(name = "balance", nullable = false)
    val balance: Long = 0L,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),

    // 간단한 동시성 제어용 optimistic lock
    @Version
    val version: Long? = null
)
