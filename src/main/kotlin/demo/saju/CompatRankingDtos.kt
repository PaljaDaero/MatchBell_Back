package demo.saju

data class CompatRankingItem(
    val rank: Int,
    val userAId: Long,
    val userBId: Long,
    val userANickname: String?,
    val userBNickname: String?,
    val finalScore: Double,
    val stressScore: Double,
    val compositeScore: Int   // 새로 추가: MatchingScore 기반 최종 점수 C
)

data class CompatRankingResponse(
    val items: List<CompatRankingItem>,

    // 내 최고 궁합 점수 (모든 페어 중 최댓값, C 기준)
    val myBestCompositeScore: Int?,   

    // 내 최고 점수가 전체 유저-페어 점수 중 상위 몇 % 인지 (예: 7.5 -> 상위 7.5%)
    val myPercentile: Double?
)
