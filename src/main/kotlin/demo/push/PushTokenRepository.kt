package demo.push

import demo.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PushTokenRepository : JpaRepository<PushTokenEntity, Long> {

    fun findByUser(user: UserEntity): List<PushTokenEntity>

    fun findByUserId(userId: Long): List<PushTokenEntity>

    fun findByUserIdAndIsActiveTrue(userId: Long): List<PushTokenEntity>

    fun findByToken(token: String): PushTokenEntity?
}
