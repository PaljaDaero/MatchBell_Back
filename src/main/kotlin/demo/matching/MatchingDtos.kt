package demo.matching

import demo.profile.Gender
import demo.saju.CompatResponse
import java.time.LocalDate
import java.time.LocalDateTime

data class CuriousResponse(
    val status: String,   // "SENT" or "MATCHED"
    val matched: Boolean,
    val matchId: Long?
)

data class CuriousUserSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val createdAt: LocalDateTime
)

data class MatchSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val age: Int,
    val region: String?,
    val job: String?,
    val matchedAt: LocalDateTime
)

/**
 * 매칭된 상대 프로필 + 궁합 결과
 */
data class MatchProfileResponse(
    val userId: Long,
    val nickname: String,
    val intro: String?,
    val gender: Gender,
    val birth: LocalDate,
    val region: String?,
    val job: String?,
    val avatarUrl: String?,
    val tendency: String?,
    val compat: CompatResponse
)
