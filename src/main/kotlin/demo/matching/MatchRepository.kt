package demo.matching

import demo.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface MatchRepository : JpaRepository<MatchEntity, Long> {

    /**
     * user1.id < user2.id 규칙을 지켜서 검색해야 함
     */
    fun findByUser1AndUser2(user1: UserEntity, user2: UserEntity): MatchEntity?

    /**
     * 내가 포함된 매칭 모두 조회
     */
    fun findByUser1IdOrUser2Id(userId1: Long, userId2: Long): List<MatchEntity>
}
