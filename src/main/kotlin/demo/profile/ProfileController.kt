package demo.profile

import demo.auth.JwtTokenProvider
import demo.file.FileStorageService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/me")
class ProfileController(
    private val profileRepository: ProfileRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val fileStorageService: FileStorageService
) {

    /**
     * 내 프로필 조회
     * GET /me/profile
     */
    @GetMapping("/profile")
    fun getMyProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): ProfileResponse {
        val userId = extractUserIdFromHeader(authHeader)
        val profile = profileRepository.findByUserId(userId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "프로필을 찾을 수 없습니다."
            )

        return profile.toResponse()
    }

    /**
     * 내 프로필 수정
     * PATCH /me/profile
     * - 닉네임, 자기소개, 지역, 직업, 사진만 수정 가능
     * - 성별, 생년월일은 수정 불가
     */
    @PatchMapping("/profile")
    fun updateMyProfile(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody req: ProfileUpdateRequest
    ): ProfileResponse {
        val userId = extractUserIdFromHeader(authHeader)
        val profile = profileRepository.findByUserId(userId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "프로필을 찾을 수 없습니다."
            )

        
        val updated = profile.copy(
            nickname = req.nickname ?: profile.nickname,
            intro = req.intro ?: profile.intro,
            region = req.region ?: profile.region,
            job = req.job ?: profile.job,
            avatarUrl = req.avatarUrl ?: profile.avatarUrl
        )

        val saved = profileRepository.save(updated)
        return saved.toResponse()
    }

    /**
     * 프로필 사진 업로드
     * POST /me/profile/image
     * - form-data: file=<이미지>
     */
    @PostMapping(
        "/profile/image",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]
    )
    fun uploadProfileImage(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestPart("file") file: MultipartFile
    ): ProfileResponse {
        val userId = extractUserIdFromHeader(authHeader)

        val profile = profileRepository.findByUserId(userId)
            ?: throw ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "프로필을 찾을 수 없습니다."
            )

        // 파일 저장
        val avatarUrl = try {
            fileStorageService.saveProfileImage(userId, file)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                e.message ?: "잘못된 파일입니다."
            )
        }

        // 프로필 업데이트
        val updatedProfile = profile.copy(
            avatarUrl = avatarUrl
        )
        val saved = profileRepository.save(updatedProfile)

        return saved.toResponse()
    }

    /**
     * Authorization 헤더에서 Bearer 토큰 추출 후 userId 파싱
     */
    private fun extractUserIdFromHeader(authHeader: String?): Long {
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "인증 정보가 없습니다."
            )
        }

        val token = authHeader.removePrefix("Bearer ").trim()
        val userId = jwtTokenProvider.parseUserId(token)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "유효하지 않은 토큰입니다."
            )

        return userId
    }

    /**
     * 엔티티 → 응답 DTO 변환
     */
    private fun ProfileEntity.toResponse(): ProfileResponse =
        ProfileResponse(
            nickname = this.nickname,
            intro = this.intro,
            gender = this.gender,
            birth = this.birthDate,
            region = this.region,
            job = this.job,
            avatarUrl = this.avatarUrl,
            tendency = this.tendency
        )
}
