package demo.location

import demo.auth.JwtTokenProvider
import demo.profile.Gender
import demo.profile.ProfileEntity
import demo.profile.ProfileRepository
import demo.saju.SajuMatchRequest
import demo.saju.SajuPersonPayload
import demo.saju.SajuPythonClient
import demo.saju.SajuTendencyUtil
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import kotlin.math.*

@RestController
@RequestMapping("/radar")
class RadarController(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val userLocationRepository: UserLocationRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val sajuPythonClient: SajuPythonClient
) {

    // 최근 위치 정보 유효 시간 (분)
    private val LOCATION_TIMEOUT_MINUTES = 10L

    /**
     * GET /radar?radiusMeters=2000&limit=20
     *
     * - Authorization: Bearer <JWT> 
     * - radiusMeters: 반경
     * - limit: 20명
     */
    @GetMapping
    fun getRadar(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestParam(name = "radiusMeters", defaultValue = "2000") radiusMeters: Double,
        @RequestParam(name = "limit", defaultValue = "20") limit: Int
    ): RadarResponse {
        val userId = extractUserIdFromHeader(authHeader)

        // 1) Profile + Location
        val myProfile = profileRepository.findByUserId(userId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "내 프로필을 찾을 수 없습니다."
            )

        val myLocation = userLocationRepository.findByUserId(userId)
            ?: throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "내 위치 정보가 없습니다. 먼저 /me/location 으로 위치를 업데이트 해주세요."
            )

        val now = LocalDateTime.now()
        if (myLocation.updatedAt.isBefore(now.minusMinutes(LOCATION_TIMEOUT_MINUTES))) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "내 위치 정보가 오래되었습니다. /me/location 으로 다시 위치를 업데이트 해주세요."
            )
        }

        val myLat = myLocation.lat
        val myLng = myLocation.lng

        // 2) 모든 사용자 위치 조회 + 나 제외 + 최근 위치 필터링
        val allLocations = userLocationRepository.findAll()

        val candidateLocations = allLocations
            .filter { it.user.id != userId }
            .filter { it.updatedAt.isAfter(now.minusMinutes(LOCATION_TIMEOUT_MINUTES)) }

        // 3) 거리 계산 → 반경 + limit 필터링
        val candidatesWithDistance = candidateLocations
            .map { loc ->
                val dist = haversineDistanceMeters(
                    myLat, myLng,
                    loc.lat, loc.lng
                )
                loc to dist
            }
            .filter { (_, dist) -> dist <= radiusMeters }
            .sortedBy { (_, dist) -> dist }
            .take(limit)

        if (candidatesWithDistance.isEmpty()) {
            return RadarResponse(
                me = RadarMeDto(
                    lat = myLat,
                    lng = myLng,
                    region = myProfile.region
                ),
                users = emptyList()
            )
        }

        // 4) 궁합 계산 + 결과 조합
        val myPayload = myProfile.toSajuPersonPayload()

        val radarUsers = candidatesWithDistance.mapNotNull { (loc, dist) ->
            val otherProfile = profileRepository.findByUserId(loc.user.id!!)
                ?: return@mapNotNull null


            if (!isOppositeGender(myProfile.gender, otherProfile.gender)) {
                return@mapNotNull null
            }

            val otherPayload = otherProfile.toSajuPersonPayload()

            // 사주 궁합 계산 요청
            val matchResult = sajuPythonClient.match(
                SajuMatchRequest(
                    person0 = myPayload,
                    person1 = otherPayload
                )
            )

            val tendency0 = SajuTendencyUtil.fromSal(matchResult.sal0)
            val tendency1 = SajuTendencyUtil.fromSal(matchResult.sal1)

            RadarUserDto(
                userId = otherProfile.user.id!!,
                nickname = otherProfile.nickname,
                gender = otherProfile.gender,
                age = otherProfile.toAgeOrNull(),
                distanceMeters = dist,
                region = otherProfile.region,
                avatarUrl = otherProfile.avatarUrl,
                originalScore = matchResult.originalScore,
                finalScore = matchResult.finalScore,
                stressScore = matchResult.stressScore,
                tendency0 = tendency0,
                tendency1 = tendency1
            )

        }

        val meDto = RadarMeDto(
            lat = myLat,
            lng = myLng,
            region = myProfile.region
        )

        return RadarResponse(
            me = meDto,
            users = radarUsers
        )
    }

    /**
     * 두 위도/경도 간의 거리 계산 (미터 단위)
     */
    private fun haversineDistanceMeters(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double
    ): Double {
        val R = 6371000.0 // 지구 반경 (미터)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) *
                cos(Math.toRadians(lat2)) *
                sin(dLng / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    /**
     * JWT → userId
     */
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

    /**
     * ProfileEntity → SajuPersonPayload
     * (사주 엔진에 넘길 형식)
     */
    private fun ProfileEntity.toSajuPersonPayload(): SajuPersonPayload {
        val genderInt = when (this.gender) {
            Gender.MALE -> 1
            Gender.FEMALE -> 0
            Gender.OTHER -> 1 // 필요시 조정
        }

        return SajuPersonPayload(
            year = this.birthDate.year,
            month = this.birthDate.monthValue,
            day = this.birthDate.dayOfMonth,
            gender = genderInt
        )
    }

    private fun isOppositeGender(me: Gender, other: Gender): Boolean =
    when (me) {
        Gender.MALE -> other == Gender.FEMALE
        Gender.FEMALE -> other == Gender.MALE
        Gender.OTHER -> false   // TODO: 필요시 조정
    }

    /**
     * ProfileEntity → age (or null)
     */
    private fun ProfileEntity.toAgeOrNull(): Int? {
        val birth = this.birthDate ?: return null
        return try {
            Period.between(birth, LocalDate.now()).years
        } catch (e: Exception) {
            null
        }
    }
}
