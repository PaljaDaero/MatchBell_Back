package demo.cookie

import demo.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface CookieWalletRepository : JpaRepository<CookieWalletEntity, Long> {
    fun findByUser(user: UserEntity): CookieWalletEntity?
    fun findByUserId(userId: Long): CookieWalletEntity?
}

interface CookieTransactionRepository : JpaRepository<CookieTransactionEntity, Long> {
    fun findTop20ByWalletOrderByCreatedAtDesc(wallet: CookieWalletEntity): List<CookieTransactionEntity>
}
