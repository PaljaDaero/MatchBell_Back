package demo.cookie

import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@Service
class CookieService(
    private val userRepository: UserRepository,
    private val walletRepository: CookieWalletRepository,
    private val txRepository: CookieTransactionRepository
) {

    /**
     * 지갑 조회 또는 생성
     */
    @Transactional
    fun getOrCreateWallet(userId: Long): CookieWalletEntity {
        val existing = walletRepository.findByUserId(userId)
        if (existing != null) return existing

        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }

        val wallet = CookieWalletEntity(
            user = user,
            balance = 0L,
            updatedAt = LocalDateTime.now()
        )
        return walletRepository.save(wallet)
    }

    @Transactional(readOnly = true)
    fun getBalance(userId: Long): Long {
        val wallet = walletRepository.findByUserId(userId)
            ?: return 0L
        return wallet.balance
    }

    /**
     * 충전/획득
     */
    @Transactional
    fun earn(userId: Long, amount: Long, reason: String? = null): CookieWalletEntity {
        if (amount <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다.")
        }

        val wallet = getOrCreateWallet(userId)
        val newBalance = wallet.balance + amount

        val updated = wallet.copy(
            balance = newBalance,
            updatedAt = LocalDateTime.now()
        )
        val saved = walletRepository.save(updated)

        val tx = CookieTransactionEntity(
            wallet = saved,
            type = CookieTransactionType.EARN,
            amount = amount,
            balanceAfter = newBalance,
            reason = reason
        )
        txRepository.save(tx)

        return saved
    }

    /**
     * 쿠키 차감/사용 (예: 아이템 구매 등)
     */
    @Transactional
    fun spend(userId: Long, amount: Long, reason: String? = null): CookieWalletEntity {
        if (amount <= 0) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "금액은 0보다 커야 합니다.")
        }

        val wallet = getOrCreateWallet(userId)
        if (wallet.balance < amount) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "쿠키가 부족합니다.")
        }

        val newBalance = wallet.balance - amount

        val updated = wallet.copy(
            balance = newBalance,
            updatedAt = LocalDateTime.now()
        )
        val saved = walletRepository.save(updated)

        val tx = CookieTransactionEntity(
            wallet = saved,
            type = CookieTransactionType.SPEND,
            amount = amount,
            balanceAfter = newBalance,
            reason = reason
        )
        txRepository.save(tx)

        return saved
    }

    @Transactional(readOnly = true)
    fun getRecentTransactions(userId: Long): List<CookieTransactionEntity> {
        val wallet = walletRepository.findByUserId(userId) ?: return emptyList()
        return txRepository.findTop20ByWalletOrderByCreatedAtDesc(wallet)
    }
}
