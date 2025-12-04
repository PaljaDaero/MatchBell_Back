package demo.saju

import demo.auth.JwtTokenProvider
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
    private val compatRecordRepository: CompatRecordRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    // --------------------------------------------------------------------
    // 기존 점수 계산 API (DB 저장 부분은 그대로 둬도 됨)
    // --------------------------------------------------------------------
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

        val matchResult = sajuPythonClient.match(pythonReq)

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

        // 궁합 결과 DB 기록 (랭킹/통계용)
        val meUser = meProfile.user
        val targetUser = targetProfile.user
        val (userA, userB) =
            if ((meUser.id ?: 0L) <= (targetUser.id ?: 0L)) {
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

    // --------------------------------------------------------------------
    // 새 랭킹 API
    //   - CompositeScore(C) 로 정렬
    //   - 같은 페어 중복 제거 (최대 C 사용)
    //   - 현재 내 최고 점수 + 상위 % 계산
    // GET /compat/ranking?limit=100
    // Authorization: Bearer <JWT>
    // --------------------------------------------------------------------
    @GetMapping("/ranking")
    fun getRanking(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestParam("limit", required = false, defaultValue = "100") limit: Int
    ): CompatRankingResponse {

        val myUserId = extractUserIdFromHeader(authHeader)

        val allRecords = compatRecordRepository.findAll()
        if (allRecords.isEmpty()) {
            return CompatRankingResponse(
                items = emptyList(),
                myBestCompositeScore = null,
                myPercentile = null
            )
        }

        val matchingScore = MatchingScore()

        // 1) (userA, userB) / (userB, userA) 중복 제거.
        //    key: (minId, maxId)
        data class RecordWithComposite(
            val record: CompatRecordEntity,
            val compositeScore: Int
        )

        val pairBestMap = LinkedHashMap<Pair<Long, Long>, RecordWithComposite>()

        for (record in allRecords) {
            val userAId = record.userA.id ?: continue
            val userBId = record.userB.id ?: continue

            val key = if (userAId <= userBId) {
                userAId to userBId
            } else {
                userBId to userAId
            }

            val composite = matchingScore.calculateCompositeScore(
                record.finalScore,
                record.stressScore
            )

            val existing = pairBestMap[key]
            if (existing == null || composite > existing.compositeScore) {
                pairBestMap[key] = RecordWithComposite(record, composite)
            }
        }

        val dedupList = pairBestMap.values.toList()
        if (dedupList.isEmpty()) {
            return CompatRankingResponse(
                items = emptyList(),
                myBestCompositeScore = null,
                myPercentile = null
            )
        }

        // 2) CompositeScore 기준 내림차순 정렬
        val sortedByComposite = dedupList.sortedByDescending { it.compositeScore }

        // 3) 상위 N개를 랭킹 아이템으로 변환
        val items = sortedByComposite
            .take(limit)
            .mapIndexed { index, wrapper ->
                val record = wrapper.record
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
                    stressScore = record.stressScore,
                    compositeScore = wrapper.compositeScore
                )
            }

        // 4) "현재 내 점수" = 내가 참여한 페어 중 compositeScore 최대값
        val myBest = dedupList
            .filter {
                val r = it.record
                r.userA.id == myUserId || r.userB.id == myUserId
            }
            .maxByOrNull { it.compositeScore }

        val myBestCompositeScore = myBest?.compositeScore

        // 5) 내 최고 점수의 랭킹과 상위 퍼센트 계산
        val totalPairs = dedupList.size
        val myPercentile: Double? = myBestCompositeScore?.let { score ->
            val rankIndex = sortedByComposite.indexOfFirst { it.compositeScore == score }
            if (rankIndex == -1) {
                null
            } else {
                // rankIndex는 0-based 이므로 +1
                val rank = rankIndex + 1
                // "상위 몇 %" → (랭크 / 전체) * 100
                (rank.toDouble() / totalPairs.toDouble()) * 100.0
            }
        }

        return CompatRankingResponse(
            items = items,
            myBestCompositeScore = myBestCompositeScore,
            myPercentile = myPercentile
        )
    }

    // --------------------------------------------------------------------
    // 기존에 다른 컨트롤러에서도 쓰던 JWT 유저 ID 추출 로직 재사용
    // --------------------------------------------------------------------
    private fun extractUserIdFromHeader(authHeader: String?): Long {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.")
        }
        val token = authHeader.substringAfter("Bearer ").trim()
        val userId = jwtTokenProvider.getUserIdFromToken(token)
        return userId ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다.")
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
