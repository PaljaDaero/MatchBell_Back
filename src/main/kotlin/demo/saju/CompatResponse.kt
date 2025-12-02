package demo.saju

/**
 *  - tendency0: 첫번째 사람 성향분석
 *  - tendency1: 두번째 사람 성향분석
 */
data class CompatResponse(
    val originalScore: Double,
    val finalScore: Double,
    val stressScore: Double,
    val sal0: List<Double>,
    val sal1: List<Double>,
    val person0: List<Int>,
    val person1: List<Int>,
    val tendency0: List<String>,
    val tendency1: List<String>
)
