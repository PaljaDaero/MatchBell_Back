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

/**
 * 궁금해요를 받은 사용자 요약 정보
 */
data class CuriousUserSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val createdAt: LocalDateTime
)

/**
 * 매칭된 사용자 요약 정보
 */
data class MatchSummary(
    val userId: Long,
    val nickname: String,
    val avatarUrl: String?,
    val age: Int,
    val region: String?,
    val job: String?,
    val matchedAt: LocalDateTime
)


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

/**
 * 프로필 잠금 해제 결과
 */
data class ProfileUnlockResponse(
    val unlocked: Boolean,      
    val alreadyUnlocked: Boolean, 
    val cost: Long,              
    val balanceAfter: Long?  
)
