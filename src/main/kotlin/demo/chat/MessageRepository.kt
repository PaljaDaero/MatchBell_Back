package demo.chat

import org.springframework.data.jpa.repository.JpaRepository

interface MessageRepository : JpaRepository<MessageEntity, Long> {
    // 특정 매치의 모든 메세지를 보낸 시간 순서대로 가져오기
    fun findByMatchIdOrderBySentAtAsc(matchId: Long): List<MessageEntity>
    // 특정 매치의 가장 최근 메세지 1개 가져오기
    fun findTop1ByMatchIdOrderBySentAtDesc(matchId: Long): MessageEntity?
    // 특정 매치에서 특정 사용자가 받지 않은 메세지 개수 세기
    fun countByMatchIdAndSenderIdNotAndStatus(
        matchId: Long,
        senderId: Long,
        status: MessageStatus
    ): Long
}
