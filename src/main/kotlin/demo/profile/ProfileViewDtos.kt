package demo.profile

import demo.saju.CompatResponse
import java.time.LocalDate

/**
 * 프로필 상세 조회 응답 (완전 플랫 구조, 잠금/해제 서버 로직 제거)
 *
 * 예시 JSON:
 * {
 *   "userId": 2,
 *   "nickname": "테스트2",
 *   "intro": "게임 좋아합니다.",
 *   "gender": "MALE",
 *   "birth": "1998-11-03",
 *   "region": "서울시 마포구",
 *   "job": "개발자",
 *   "avatarUrl": "https://cdn.example.com/profile/2.png",
 *   "tendency": "열정 에너지 / 책임감 의리",
 *   "compat": {
 *     "originalScore": 78.79902648925781,
 *     "finalScore": 78.79902648925781,
 *     "stressScore": 13.600486755371094,
 *     "sal0": [...],
 *     "sal1": [...],
 *     "person0": [6,2,3,3,1,1],
 *     "person1": [5,1,1,3,1,3],
 *     "tendency0": ["열정 에너지 예술 중독"],
 *     "tendency1": ["책임감 의리 완벽 자존심 인내"]
 *   }
 * }
 */
data class ProfileViewResponse(
    val userId: Long,
    val nickname: String,
    val intro: String?,
    val gender: Gender,
    val birth: LocalDate,
    val region: String?,
    val job: String?,
    val avatarUrl: String?,
    val tendency: String?,
    val compat: CompatResponse?   // 자기 자신일 때는 null, 그 외엔 항상 계산해서 내려줌
)
