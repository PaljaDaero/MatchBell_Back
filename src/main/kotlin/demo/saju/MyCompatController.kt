package demo.saju

import demo.auth.JwtTokenProvider
import demo.profile.Gender
import demo.profile.ProfileEntity
import demo.profile.ProfileRepository
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeParseException

@RestController
@RequestMapping("/my-compat")
class MyCompatController(
    private val jwtTokenProvider: JwtTokenProvider,
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val sajuPythonClient: SajuPythonClient,
    private val myCompatRecordRepository: MyCompatRecordRepository
) {

    /**
     * 나만의 궁합 계산
     * POST /my-compat
     */
    @PostMapping
    fun calculateMyCompat(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody req: MyCompatRequest
    ): MyCompatResultResponse {
        val userId = extractUserIdFromHeader(authHeader)

        val meProfile = profileRepository.findByUserId(userId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "내 프로필을 찾을 수 없습니다."
            )

        val targetBirth = try {
            LocalDate.parse(req.birth)
        } catch (e: DateTimeParseException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "생년월일 형식은 yyyy-MM-dd 여야 합니다."
            )
        }

        val mePayload = meProfile.toSajuPersonPayload()
        val targetPayload = SajuPersonPayload(
            year = targetBirth.year,
            month = targetBirth.monthValue,
            day = targetBirth.dayOfMonth,
            gender = toPythonGender(req.gender)
        )

        val pythonReq = SajuMatchRequest(
            person0 = mePayload,
            person1 = targetPayload
        )

        val matchResult = sajuPythonClient.match(pythonReq)

        val tendency0 = SajuTendencyUtil.fromSal(matchResult.sal0)
        val tendency1 = SajuTendencyUtil.fromSal(matchResult.sal1)

        val compat = CompatResponse(
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

        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }

        // 히스토리 저장
        myCompatRecordRepository.save(
            MyCompatRecordEntity(
                user = user,
                targetName = req.name,
                targetGender = req.gender,
                targetBirth = targetBirth,
                finalScore = matchResult.finalScore,
                stressScore = matchResult.stressScore
            )
        )

        return MyCompatResultResponse(
            targetName = req.name,
            targetGender = req.gender,
            targetBirth = targetBirth,
            compat = compat
        )
    }

    /**
     * 나만의 궁합 히스토리
     * GET /my-compat/history
     */
    @GetMapping("/history")
    fun getHistory(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): MyCompatHistoryResponse {
        val userId = extractUserIdFromHeader(authHeader)

        val records = myCompatRecordRepository
            .findTop50ByUserIdOrderByCreatedAtDesc(userId)

        val items = records.map { rec ->
            val id = rec.id ?: error("MyCompatRecordEntity id is null")
            MyCompatHistoryItem(
                id = id,
                targetName = rec.targetName,
                targetGender = rec.targetGender,
                targetBirth = rec.targetBirth,
                finalScore = rec.finalScore,
                stressScore = rec.stressScore,
                createdAt = rec.createdAt
            )
        }


        return MyCompatHistoryResponse(items)
    }

    private fun extractUserIdFromHeader(authHeader: String?): Long {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "인증 정보가 없습니다."
            )
        }
        val token = authHeader.removePrefix("Bearer ").trim()
        return jwtTokenProvider.parseUserId(token)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "유효하지 않은 토큰입니다."
            )
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
