package demo.chat

import demo.auth.JwtTokenProvider
import demo.matching.MatchRepository
import demo.matching.MatchStatus
import demo.profile.ProfileRepository
import org.springframework.http.HttpStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDateTime

@RestController
@RequestMapping("/me")
class ChatRestController(
    private val matchRepository: MatchRepository,
    private val messageRepository: MessageRepository,
    private val profileRepository: ProfileRepository,
    private val jwtTokenProvider: JwtTokenProvider
) {

    /**
     * 채팅 리스트
     * GET /me/chats
     *
     * - 현재 로그인한 사용자가 참여한 모든 매칭 목록
     * - 각 매칭에 대해:
     *    - 상대 프로필 (닉네임, 아바타)
     *    - 마지막 메세지 내용/시간
     *    - unreadCount: 내가 아직 읽지 않은 메세지 수
     */
    @GetMapping("/chats")
    fun getMyChatRooms(
        @RequestHeader("Authorization", required = false) authHeader: String?
    ): List<ChatRoomItemResponse> {
        val userId = extractUserIdFromHeader(authHeader)

        // 내가 user1 또는 user2 인 매칭만 조회
        val matches = matchRepository.findByUser1IdOrUser2Id(userId, userId)
            .filter { it.status == MatchStatus.ACTIVE }

        return matches.map { match ->
            val otherUser = if (match.user1.id == userId) match.user2 else match.user1

            val profile = profileRepository.findByUserId(otherUser.id!!)
            val lastMsg = messageRepository.findTop1ByMatchIdOrderBySentAtDesc(match.id)

            // 내가 아직 읽지 않은 메세지 수:
            //  - matchId = match.id
            //  - sender != 나
            //  - status = SENT
            val unreadCount = messageRepository.countByMatchIdAndSenderIdNotAndStatus(
                matchId = match.id,
                senderId = userId,
                status = MessageStatus.SENT
            )

            ChatRoomItemResponse(
                matchId = match.id,
                otherUserId = otherUser.id!!,
                otherNickname = profile?.nickname ?: "(알 수 없음)",
                otherAvatarUrl = profile?.avatarUrl,
                lastMessage = lastMsg?.content,
                lastMessageTime = lastMsg?.sentAt,
                unreadCount = unreadCount
            )
        }.sortedByDescending { it.lastMessageTime ?: LocalDateTime.MIN }
    }

    /**
     * 특정 방의 히스토리 + 읽음 처리
     * GET /me/chats/{matchId}/messages
     *
     * - 내가 참여한 방인지 권한 체크
     * - 그 방의 메세지 전체를 시간 순으로 반환
     * - 동시에 "내가 받은 SENT 메세지" 들을 READ 로 변경
     */
    @GetMapping("/chats/{matchId}/messages")
    @Transactional
    fun getChatMessages(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable matchId: Long
    ): List<ChatMessageResponse> {
        val userId = extractUserIdFromHeader(authHeader)

        val match = matchRepository.findById(matchId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "매칭 정보를 찾을 수 없습니다.")
        }

        val isParticipant =
            (match.user1.id == userId) || (match.user2.id == userId)
        if (!isParticipant) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "채팅 접근 권한이 없습니다."
            )
        }

        if (match.status == MatchStatus.BLOCKED) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "차단된 대화방입니다."
            )
        }

        // 1) 방의 모든 메세지 조회
        val messages = messageRepository.findByMatchIdOrderBySentAtAsc(matchId)


        // 2) 내가 받은 + 아직 SENT 상태인 메세지들을 READ 로 변경
        val toUpdate = messages
            .filter { msg ->
                msg.sender.id != userId && msg.status == MessageStatus.SENT
            }
            .map { msg ->
                msg.copy(status = MessageStatus.READ)
            }

        if (toUpdate.isNotEmpty()) {
            messageRepository.saveAll(toUpdate)
        }

        // 3) 업데이트 결과를 반영해서 응답 DTO 로 변환
        val updatedMap = toUpdate.associateBy { it.id }

        return messages.map { msg ->
            val finalMsg = updatedMap[msg.id] ?: msg
            ChatMessageResponse(
                id = finalMsg.id,
                matchId = match.id,
                senderId = finalMsg.sender.id!!,
                content = finalMsg.content,
                sentAt = finalMsg.sentAt,
                status = finalMsg.status
            )
        }
    }

    /**
     * 채팅 차단
     * POST /me/chats/{matchId}/block
     *
     * - 내가 속한 매칭인지 확인
     * - match.status 를 BLOCKED 로 변경
     * - 이후 채팅 리스트/메시지 조회/발송 모두 제한
     */
    @PostMapping("/chats/{matchId}/block")
    @Transactional
    fun blockChat(
        @RequestHeader("Authorization", required = false) authHeader: String?,
        @PathVariable matchId: Long
    ) {
        val userId = extractUserIdFromHeader(authHeader)

        val match = matchRepository.findById(matchId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "매칭 정보를 찾을 수 없습니다.")
        }

        val isParticipant =
            (match.user1.id == userId) || (match.user2.id == userId)
        if (!isParticipant) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "차단 권한이 없습니다."
            )
        }

        if (match.status == MatchStatus.BLOCKED) {
            // 이미 차단된 상태면 그냥 종료
            return
        }

        val blocked = match.copy(status = MatchStatus.BLOCKED)
        matchRepository.save(blocked)
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
        return jwtTokenProvider.parseUserId(token)
            ?: throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "유효하지 않은 토큰입니다."
            )
    }
}
