package demo.location

import demo.auth.JwtTokenProvider
import demo.profile.ProfileRepository
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
@RequestMapping("/me")
class LocationController(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val userLocationRepository: UserLocationRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * 내 위치 업데이트
     * POST /me/location
     * Authorization: Bearer <JWT>
     *
     * body: { "lat": 37.1234, "lng": 127.1234, "region": "서울시 마포구" }
     */
    @PostMapping("/location")
    fun updateMyLocation(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody req: LocationUpdateRequest
    ) {
        val userId = extractUserIdFromHeader(authHeader)

        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }

        // 1) 위치 정보 upsert (있으면 수정, 없으면 생성)
        val existing = userLocationRepository.findByUser(user)

        val entity = if (existing == null) {
            // 새로 생성
            UserLocationEntity(
                user = user,
                lat = req.lat,
                lng = req.lng,
                updatedAt = LocalDateTime.now()
            )
        } else {
            // data class + val → copy 로 새 인스턴스 생성
            existing.copy(
                lat = req.lat,
                lng = req.lng,
                updatedAt = LocalDateTime.now()
            )
        }

        userLocationRepository.save(entity)

        // 2) region 이 들어온 경우, Profile.region 도 같이 업데이트
        if (!req.region.isNullOrBlank()) {
            val profile = profileRepository.findByUserId(userId)
                ?: throw ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "프로필을 찾을 수 없습니다."
                )

            val updatedProfile = profile.copy(
                region = req.region
            )
            profileRepository.save(updatedProfile)
        }
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
}
