package demo.saju

import org.springframework.data.jpa.repository.JpaRepository

interface CompatRecordRepository : JpaRepository<CompatRecordEntity, Long> {

    fun findTop100ByOrderByFinalScoreDesc(): List<CompatRecordEntity>
}
