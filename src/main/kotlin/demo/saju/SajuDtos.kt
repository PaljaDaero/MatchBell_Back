package demo.saju

// Python 사주 인물 정보 DTO
data class SajuPersonPayload(
    val year: Int,
    val month: Int,
    val day: Int,
    val gender: Int   // 1 = 남자, 0 = 여자
)

// Python 궁합 요청 DTO
data class SajuMatchRequest(
    val person0: SajuPersonPayload,
    val person1: SajuPersonPayload,
)

// Python 응답 DTO
data class SajuMatchResult(
    val originalScore: Double,
    val finalScore: Double,
    val stressScore: Double,
    val sal0: List<Double>,
    val sal1: List<Double>,
    val person0: List<Int>,
    val person1: List<Int>,
)
