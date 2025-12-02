package demo.matching

import demo.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface LikeRepository : JpaRepository<LikeEntity, Long> {

    fun findByFromUserAndToUser(fromUser: UserEntity, toUser: UserEntity): LikeEntity?

    fun findByFromUserIdAndStatus(fromUserId: Long, status: LikeStatus): List<LikeEntity>

    fun findByToUserIdAndStatus(toUserId: Long, status: LikeStatus): List<LikeEntity>
}
