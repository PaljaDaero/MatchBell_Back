package demo.matching

import demo.chat.MessageEntity
import demo.chat.MessageRepository
import demo.profile.ProfileRepository
import demo.user.UserEntity
import demo.user.UserRepository
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

@Service
class MatchingService(
    private val userRepository: UserRepository,
    private val likeRepository: LikeRepository,
    private val matchRepository: MatchRepository,
    private val profileRepository: ProfileRepository,
    private val messageRepository: MessageRepository        // 🔹 새로 주입
) {

    /**
     * 궁금해요 보내기
     * - 이미 ACTIVE 상태로 보냈으면 그대로 유지 (idempotent)
     * - 상대도 나에게 ACTIVE로 보냈으면 매칭 성립 → matches에 1번만 기록
     *   + 매칭이 처음 성사될 때 자동 환영 메세지 1개 생성
     */
    @Transactional
    fun sendCurious(fromUserId: Long, toUserId: Long): CuriousResponse {
        if (fromUserId == toUserId) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "자기 자신에게는 궁금해요를 보낼 수 없습니다."
            )
        }

        val fromUser = userRepository.findById(fromUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "보낸 사용자를 찾을 수 없습니다.")
        }
        val toUser = userRepository.findById(toUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "상대 사용자를 찾을 수 없습니다.")
        }

        val existing = likeRepository.findByFromUserAndToUser(fromUser, toUser)
        if (existing != null && existing.status == LikeStatus.ACTIVE) {
            val reverse = likeRepository.findByFromUserAndToUser(toUser, fromUser)
            val matched = reverse != null && reverse.status == LikeStatus.ACTIVE
            val matchId = if (matched) ensureMatchExists(fromUser, toUser) else null
            return CuriousResponse(
                status = if (matched) "MATCHED" else "SENT",
                matched = matched,
                matchId = matchId
            )
        }

        // 아직 ACTIVE가 아니면 새로 저장 또는 재활성화
        val like = existing?.copy(
            status = LikeStatus.ACTIVE,
            createdAt = LocalDateTime.now()
        ) ?: LikeEntity(
            fromUser = fromUser,
            toUser = toUser,
            status = LikeStatus.ACTIVE
        )
        likeRepository.save(like)

        val reverse = likeRepository.findByFromUserAndToUser(toUser, fromUser)
        val matched = reverse != null && reverse.status == LikeStatus.ACTIVE
        val matchId = if (matched) ensureMatchExists(fromUser, toUser) else null

        return CuriousResponse(
            status = if (matched) "MATCHED" else "SENT",
            matched = matched,
            matchId = matchId
        )
    }

    /**
     * 양방향 like 가 ACTIVE 일 때 matches 테이블에 한 번만 레코드 생성
     * + 처음 생성될 때 자동으로 웰컴 메세지 1개 생성
     *
     * @param fromUser  지금 궁금해요를 보낸 사람 (웰컴 메세지 발신자로 사용)
     * @param toUser    상대방
     */
    private fun ensureMatchExists(fromUser: UserEntity, toUser: UserEntity): Long {
        // DB 저장 규칙: 항상 user1.id < user2.id
        val (u1, u2) = if ((fromUser.id ?: 0L) <= (toUser.id ?: 0L)) {
            fromUser to toUser
        } else {
            toUser to fromUser
        }

        val existing = matchRepository.findByUser1AndUser2(u1, u2)
        if (existing != null) {
            // 이미 매칭 레코드가 있으면 웰컴 메세지는 또 만들지 않음
            return existing.id
        }

        // 🔹 처음 매칭이 성사된 경우에만 match 생성
        val match = matchRepository.save(
            MatchEntity(
                user1 = u1,
                user2 = u2,
                status = MatchStatus.ACTIVE
            )
        )

        // 🔹 자동 웰컴 메세지 생성 (보낸 사람 = 지금 궁금해요를 누른 fromUser)
        createWelcomeMessage(match, fromUser)

        return match.id
    }

    /**
     * 매칭 성사 시 자동으로 한쪽이 보낸 것처럼 보이는 웰컴 메세지 생성
     */
    private fun createWelcomeMessage(
        match: MatchEntity,
        sender: UserEntity
    ) {
        val welcomeText =
            "서로 '궁금해요'를 눌러 매칭이 성사되었습니다. 가볍게 인사를 나눠보세요 😊"

        val msg = MessageEntity(
            match = match,
            sender = sender,       // 실제 유저가 보낸 것처럼 처리
            content = welcomeText
            // sentAt, status 는 기본값(LocalDateTime.now(), SENT) 사용
        )

        messageRepository.save(msg)
    }

    /**
     * 내가 보낸 궁금해요 리스트
     */
    @Transactional(readOnly = true)
    fun getSentCurious(userId: Long): List<CuriousUserSummary> {
        val likes = likeRepository.findByFromUserIdAndStatus(userId, LikeStatus.ACTIVE)

        return likes.mapNotNull { like ->
            val toUserId = like.toUser.id ?: return@mapNotNull null
            val profile = profileRepository.findByUserId(toUserId)
                ?: return@mapNotNull null   // 프로필 없으면 스킵

            CuriousUserSummary(
                userId = toUserId,
                nickname = profile.nickname,
                avatarUrl = profile.avatarUrl,
                createdAt = like.createdAt
            )
        }
    }

    /**
     * 내가 받은 궁금해요 리스트
     */
    @Transactional(readOnly = true)
    fun getReceivedCurious(userId: Long): List<CuriousUserSummary> {
        val likes = likeRepository.findByToUserIdAndStatus(userId, LikeStatus.ACTIVE)

        return likes.mapNotNull { like ->
            val fromUserId = like.fromUser.id ?: return@mapNotNull null
            val profile = profileRepository.findByUserId(fromUserId)
                ?: return@mapNotNull null

            CuriousUserSummary(
                userId = fromUserId,
                nickname = profile.nickname,
                avatarUrl = profile.avatarUrl,
                createdAt = like.createdAt
            )
        }
    }

    /**
     * 매칭 리스트 (채팅/매칭 화면용)
     */
    @Transactional(readOnly = true)
    fun getMatches(userId: Long): List<MatchSummary> {
        val matches = matchRepository
            .findByUser1IdOrUser2Id(userId, userId)
            .filter { it.status == MatchStatus.ACTIVE }

        return matches.mapNotNull { match ->
            val other = if (match.user1.id == userId) match.user2 else match.user1
            val otherId = other.id ?: return@mapNotNull null

            val profile = profileRepository.findByUserId(otherId)
                ?: return@mapNotNull null

            val age = calculateAge(profile.birthDate)

            MatchSummary(
                userId = otherId,
                nickname = profile.nickname,
                avatarUrl = profile.avatarUrl,
                age = age,
                region = profile.region,
                job = profile.job,
                matchedAt = match.createdAt
            )
        }
    }

    /**
     * meUserId 와 targetUserId 사이에 ACTIVE 매칭이 있는지 확인
     * - 없으면 403 에러
     */
    @Transactional(readOnly = true)
    fun checkHasMatch(meUserId: Long, targetUserId: Long): MatchEntity {
        val me = userRepository.findById(meUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.")
        }
        val target = userRepository.findById(targetUserId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "상대 사용자를 찾을 수 없습니다.")
        }

        val (u1, u2) = if ((me.id ?: 0L) <= (target.id ?: 0L)) me to target else target to me

        val match = matchRepository.findByUser1AndUser2(u1, u2)
            ?: throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "매칭된 사용자만 상세 프로필을 볼 수 있습니다."
            )

        if (match.status != MatchStatus.ACTIVE) {
            throw ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "종료된 매칭입니다."
            )
        }

        return match
    }

    private fun calculateAge(birthDate: LocalDate): Int =
        Period.between(birthDate, LocalDate.now()).years
}
