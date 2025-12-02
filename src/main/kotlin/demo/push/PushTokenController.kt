package demo.push

import demo.auth.JwtTokenProvider
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
@RequestMapping("/me")
class PushTokenController(
    private val userRepository: UserRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    @PostMapping("/push-token")
    fun updatePushToken(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody req: PushTokenRequest
    ) {
        val userId = extractUserIdFromHeader(authHeader)
        val user = userRepository.findById(userId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }

        val now = LocalDateTime.now()
        val platform = req.platform ?: PushPlatform.ANDROID

        // 1) 토큰이 이미 있는지 확인
        val existing = pushTokenRepository.findByToken(req.token)

        val entity = if (existing == null) {
            // 새 디바이스 토큰
            PushTokenEntity(
                user = user,
                token = req.token,
                platform = platform,
                createdAt = now,
                lastUsedAt = now,
                isActive = true
            )
        } else {
            // 기존 토큰 업데이트
            existing.copy(
                user = user,
                platform = platform,
                lastUsedAt = now,
                isActive = true
            )
        }

        pushTokenRepository.save(entity)
    }

    // 비활성화 처리
    @PostMapping("/push-token/deactivate")
    fun deactivatePushToken(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @RequestBody req: PushTokenRequest
    ) {
        val userId = extractUserIdFromHeader(authHeader)

        val existing = pushTokenRepository.findByToken(req.token)
            ?: return  // 토큰이 없으면 그냥 종료

        // 본인 토큰일 때만 비활성화
        if (existing.user.id == userId) {
            val updated = existing.copy(
                isActive = false,
                lastUsedAt = LocalDateTime.now()
            )
            pushTokenRepository.save(updated)
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
