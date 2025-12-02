package demo.saju

import demo.profile.Gender
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 나만의 궁합 요청
 */
data class MyCompatRequest(
    val name: String,
    val gender: Gender,
    val birth: String    // "yyyy-MM-dd"
)

/**
 * 나만의 궁합 결과
 */
data class MyCompatResultResponse(
    val targetName: String,
    val targetGender: Gender,
    val targetBirth: LocalDate,
    val compat: CompatResponse
)

/**
 * 나만의 궁합 히스토리 항목
 */
data class MyCompatHistoryItem(
    val id: Long,
    val targetName: String,
    val targetGender: Gender,
    val targetBirth: LocalDate,
    val finalScore: Double,
    val stressScore: Double,
    val createdAt: LocalDateTime
)

data class MyCompatHistoryResponse(
    val items: List<MyCompatHistoryItem>
)
