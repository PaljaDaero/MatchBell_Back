package demo.saju

class MatchingScore {
    private val Ws: Double = 0.7
    private val Wh: Double = 0.3

    private val T_MIN_INITIAL: Double = 5.0
    private val T_P95_INITIAL: Double = 45.0

    fun calculateCompositeScore(
        finalScore: Double,
        stressScore: Double
    ): Int {
        val S = finalScore
        val T = stressScore

        val H = calculateHarmonyScore(T, T_MIN_INITIAL, T_P95_INITIAL)
        var C = (Ws * S) + (Wh * H)
        C = applyRiskGate(S, T, C)

        return C.coerceIn(0.0, 100.0).toInt()
    }

    private fun calculateHarmonyScore(
        T: Double,
        tMin: Double,
        tP95: Double
    ): Double {
        val Tnorm: Double = if (tP95 <= tMin) {
            if (T > tMin) 1.0 else 0.0
        } else {
            val rawTnorm = (T - tMin) / (tP95 - tMin)
            rawTnorm.coerceIn(0.0, 1.0)
        }
        return (1.0 - Tnorm) * 100.0
    }

    private fun applyRiskGate(S: Double, T: Double, C: Double): Double {
        if (S <= 35.0 && T >= 40.0) {
            return C.coerceAtMost(35.0)
        }
        return C
    }
}
