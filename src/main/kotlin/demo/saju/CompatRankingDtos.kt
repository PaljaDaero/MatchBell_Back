package demo.saju

data class CompatRankingItem(
    val rank: Int,
    val userAId: Long,
    val userBId: Long,
    val userANickname: String?,
    val userBNickname: String?,
    val finalScore: Double,
    val stressScore: Double
)

data class CompatRankingResponse(
    val items: List<CompatRankingItem>
)
