package demo.push

import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

// 푸시 알림 테스트 요청 DTO
data class NotifyTestRequest(
    val targetUserId: Long,
    val title: String,
    val body: String
)

/**
 * POST /notify/test
 */
@RestController
@RequestMapping("/notify")
class NotifyTestController(
    private val userRepository: UserRepository,
    private val pushTokenRepository: PushTokenRepository,
    private val pushService: PushService
) {

    @PostMapping("/test")
    fun sendTestNotification(@RequestBody req: NotifyTestRequest) {
        val user = userRepository.findById(req.targetUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "대상 사용자를 찾을 수 없습니다.")
        }

        // 활성 토큰만 조회
        val tokens = pushTokenRepository.findByUserIdAndIsActiveTrue(user.id!!)
        if (tokens.isEmpty()) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "등록된 활성 푸시 토큰이 없습니다."
            )
        }

        tokens.forEach { tokenEntity ->
            pushService.sendToToken(
                tokenEntity.token,
                req.title,
                req.body,
                mapOf(
                "type" to "TEST",
                    "userId" to user.id!!.toString()
                )
            )
        }
    }
}
