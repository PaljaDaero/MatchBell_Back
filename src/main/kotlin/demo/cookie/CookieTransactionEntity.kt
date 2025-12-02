package demo.cookie

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cookie_transactions")
data class CookieTransactionEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "wallet_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_tx_wallet")
    )
    val wallet: CookieWalletEntity,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val type: CookieTransactionType,

    @Column(nullable = false)
    val amount: Long,  // 항상 양수, type 으로 충전/차감 의미 구분

    @Column(nullable = false)
    val balanceAfter: Long, // 이 거래 후 잔액

    @Column(length = 200)
    val reason: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class CookieTransactionType {
    EARN,   // 쿠키 획득 (이벤트, 출석, 관리자 지급 등)
    SPEND,  // 쿠키 사용 (프로필 열람, 궁금해요, 기타 기능)
    ADJUST  // 관리자 조정
}
