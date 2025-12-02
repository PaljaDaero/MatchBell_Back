package demo.saju

import demo.profile.Gender

/**
 * 개별 추천 사용자 정보
 */
data class RecommendedUser(
    val userId: Long,
    val nickname: String,
    val gender: Gender,
    val age: Int,
    val region: String?,
    val job: String?,
    val avatarUrl: String?,
    val tendency: String?,      // 개인 성향 요약
    val compatScore: Double,    // 궁합 점수(finalScore)
    val compatLevel: String,    // 점수 구간별 레벨 텍스트
    val stressScore: Double     // 관계 스트레스 지표
)

/**
 * 추천 리스트 응답
 */
data class RecommendationListResponse(
    val meUserId: Long,
    val candidates: List<RecommendedUser>
)
