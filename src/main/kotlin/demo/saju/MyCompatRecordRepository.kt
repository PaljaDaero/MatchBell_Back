package demo.saju

import org.springframework.data.jpa.repository.JpaRepository

interface MyCompatRecordRepository : JpaRepository<MyCompatRecordEntity, Long> {

    fun findTop50ByUserIdOrderByCreatedAtDesc(userId: Long): List<MyCompatRecordEntity>
}
