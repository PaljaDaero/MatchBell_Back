package demo.saju

import demo.profile.Gender
import demo.profile.ProfileEntity
import demo.profile.ProfileRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/compat")
class CompatController(
    private val profileRepository: ProfileRepository,
    private val sajuPythonClient: SajuPythonClient,
    private val compatRecordRepository: CompatRecordRepository
) {

    @PostMapping("/score")
    fun getCompatScore(
        @RequestBody req: CompatRequest
    ): CompatResponse {
        val meProfile = profileRepository.findByUserId(req.meUserId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "내 프로필을 찾을 수 없습니다."
            )

        val targetProfile = profileRepository.findByUserId(req.targetUserId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "상대 프로필을 찾을 수 없습니다."
            )

        val mePayload = meProfile.toSajuPersonPayload()
        val targetPayload = targetProfile.toSajuPersonPayload()

        val pythonReq = SajuMatchRequest(
            person0 = mePayload,
            person1 = targetPayload
        )

        // 1) Python 호출
        val matchResult = sajuPythonClient.match(pythonReq)

        // 2) 성향 텍스트
        val tendency0 = SajuTendencyUtil.fromSal(matchResult.sal0)
        val tendency1 = SajuTendencyUtil.fromSal(matchResult.sal1)

        val response = CompatResponse(
            originalScore = matchResult.originalScore,
            finalScore = matchResult.finalScore,
            stressScore = matchResult.stressScore,
            sal0 = matchResult.sal0,
            sal1 = matchResult.sal1,
            person0 = matchResult.person0,
            person1 = matchResult.person1,
            tendency0 = tendency0,
            tendency1 = tendency1
        )

        // 3) 궁합 결과 DB 기록 (랭킹/통계용)
        val meUser = meProfile.user
        val targetUser = targetProfile.user
        val (userA, userB) = if ((meUser.id ?: 0L) <= (targetUser.id ?: 0L)) {
            meUser to targetUser
        } else {
            targetUser to meUser
        }

        compatRecordRepository.save(
            CompatRecordEntity(
                userA = userA,
                userB = userB,
                finalScore = matchResult.finalScore,
                stressScore = matchResult.stressScore
            )
        )

        return response
    }

    /**
     * 전 사용자 궁합 랭킹 TOP 100
     * GET /compat/ranking?limit=100
     */
    @GetMapping("/ranking")
    fun getRanking(
        @RequestParam("limit", required = false, defaultValue = "100") limit: Int
    ): CompatRankingResponse {
        val records = compatRecordRepository.findTop100ByOrderByFinalScoreDesc()
            .take(limit)

        val items = records.mapIndexed { index, record ->
            val userAId = record.userA.id!!
            val userBId = record.userB.id!!
            val profileA = profileRepository.findByUserId(userAId)
            val profileB = profileRepository.findByUserId(userBId)

            CompatRankingItem(
                rank = index + 1,
                userAId = userAId,
                userBId = userBId,
                userANickname = profileA?.nickname,
                userBNickname = profileB?.nickname,
                finalScore = record.finalScore,
                stressScore = record.stressScore
            )
        }

        return CompatRankingResponse(items)
    }

    private fun toPythonGender(gender: Gender): Int =
        when (gender) {
            Gender.MALE -> 1
            Gender.FEMALE -> 0
            Gender.OTHER -> 1
        }

    private fun ProfileEntity.toSajuPersonPayload(): SajuPersonPayload =
        SajuPersonPayload(
            year = this.birthDate.year,
            month = this.birthDate.monthValue,
            day = this.birthDate.dayOfMonth,
            gender = toPythonGender(this.gender)
        )
}
