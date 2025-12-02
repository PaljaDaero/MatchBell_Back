package demo.saju

object SajuTendencyUtil {

    // 성향 텍스트 목록
    private val TEXTS = listOf(
        "열정 에너지 예술 중독",
        "예민 직감 영적 불안",
        "감정기복 갈등 오해 고독",
        "강함 용감 충동 변화",
        "책임감 의리 완벽 자존심 인내",
        "충돌 자유 고집",
        "카리스마 승부욕 용감 외로움",
        "의지 솔직 직설 개성 고집 독립심"
    )

    /**
     * sal 값을 성향 텍스트 리스트로 변환
     * @param sal 성향 점수 리스트
     */
    fun fromSal(sal: List<Double>): List<String> {
        val result = mutableListOf<String>()
        sal.forEachIndexed { index, value ->
            if (index < TEXTS.size && value > 0.0) {
                result += TEXTS[index]
            }
        }
        if (result.isEmpty()) {
            result += "무난"
        }
        return result
    }
}
