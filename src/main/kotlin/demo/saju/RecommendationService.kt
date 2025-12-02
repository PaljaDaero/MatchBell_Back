package demo.saju

import demo.profile.Gender
import demo.profile.ProfileEntity
import demo.profile.ProfileRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Period

@Service
class RecommendationService(
    private val profileRepository: ProfileRepository,
    private val sajuPythonClient: SajuPythonClient
) {

    /**
     * userId 기준 추천 리스트 조회
     * @param userId 현재 로그인한 사용자 ID
     * @param limit 최대 추천 인원 수 (예: 20)
     */
    fun getRecommendations(userId: Long, limit: Int = 20): RecommendationListResponse {
        val meProfile = profileRepository.findByUserId(userId)
            ?: error("프로필을 찾을 수 없습니다. userId=$userId")

        // 1) 같은 지역 후보군 조회 (region 없으면 전체에서 뽑기)
        val region = meProfile.region
        val rawCandidates: List<ProfileEntity> = if (!region.isNullOrBlank()) {
            profileRepository.findByRegion(region)
        } else {
            profileRepository.findAll()
        }

        // 2) 자기 자신 제외 + 성별이 다른 사용자만 (기본 이성 매칭)
        val filtered = rawCandidates.filter {
            it.user.id != userId && it.gender != meProfile.gender
        }

        // 3) 너무 많으면 랜덤으로 일부만 남김 (성능 보호)
        val candidateSamples = filtered.shuffled().take(50)

        // 4) 나의 사주 Payload
        val mePayload = meProfile.toSajuPersonPayload()

        // 5) 각 후보와 궁합 계산
        val scoredCandidates = candidateSamples.mapNotNull { other ->
            val otherUserId = other.user.id ?: return@mapNotNull null

            val otherPayload = other.toSajuPersonPayload()

            val matchResult = sajuPythonClient.match(
                SajuMatchRequest(
                    person0 = mePayload,
                    person1 = otherPayload
                )
            )

            val compatScore = matchResult.finalScore
            val stressScore = matchResult.stressScore
            val level = toCompatLevel(compatScore)

            val age = calculateAge(other.birthDate)

            RecommendedUser(
                userId = otherUserId,
                nickname = other.nickname,
                gender = other.gender,
                age = age,
                region = other.region,
                job = other.job,
                avatarUrl = other.avatarUrl,
                tendency = other.tendency,
                compatScore = compatScore,
                compatLevel = level,
                stressScore = stressScore
            )
        }

        // 6) 궁합 점수 내림차순 정렬 후 상위 limit개
        val top = scoredCandidates
            .sortedByDescending { it.compatScore }
            .take(limit)

        return RecommendationListResponse(
            meUserId = userId,
            candidates = top
        )
    }

    private fun ProfileEntity.toSajuPersonPayload(): SajuPersonPayload =
        SajuPersonPayload(
            year = this.birthDate.year,
            month = this.birthDate.monthValue,
            day = this.birthDate.dayOfMonth,
            gender = toPythonGender(this.gender)
        )

    private fun toPythonGender(gender: Gender): Int =
        when (gender) {
            Gender.MALE -> 1
            Gender.FEMALE -> 0
            Gender.OTHER -> 1
        }

    private fun calculateAge(birthDate: LocalDate): Int =
        Period.between(birthDate, LocalDate.now()).years

    /**
     * 점수 구간별 텍스트 레벨
     */
    private fun toCompatLevel(score: Double): String =
        when {
            score >= 85 -> "매우 높음"
            score >= 70 -> "높음"
            score >= 55 -> "보통"
            else -> "낮음"
        }
}
