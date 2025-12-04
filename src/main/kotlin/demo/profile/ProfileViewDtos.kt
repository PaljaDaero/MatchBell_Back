package demo.profile

import demo.profile.Gender
import demo.saju.CompatResponse
import java.time.LocalDate

/**
 * 이제 basic 안에 모든 정보가 들어감.
 * - 프론트가 isSelf / isMatched / hasUnlocked 에 따라
 *   어떤 필드를 보여줄지/가릴지만 결정.
 */
data class BasicProfileInfo(
    val userId: Long,
    val nickname: String,
    val age: Int,
    val region: String?,
    val avatarUrl: String?,
    val shortIntro: String?,      // 40자 요약
    val tendency: String?,        // 성향

    // 원래 detail 에 있던 것들
    val gender: Gender,
    val birth: LocalDate,
    val job: String?,
    val intro: String?,           // 전체 자기소개
    val compat: CompatResponse?   // 궁합 점수 (매칭 여부에 따라 넣을지 말지만 판단)
)

data class ProfileViewResponse(
    val basic: BasicProfileInfo,

    val isSelf: Boolean,
    val isMatched: Boolean,
    val hasUnlocked: Boolean,   // 프론트가 “해제됨” UI 보여줄지 참고용
    val canChat: Boolean,
    val canUnlock: Boolean
)
