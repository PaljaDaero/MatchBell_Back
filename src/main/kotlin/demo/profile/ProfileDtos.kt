package demo.profile

import java.time.LocalDate

/**
 * 내 프로필 조회 응답용 DTO
 */
data class ProfileResponse(
    val nickname: String,
    val intro: String?,
    val gender: Gender,
    val birth: LocalDate,
    val region: String?,
    val job: String?,
    val avatarUrl: String?,
    val tendency: String?
)

/**
 * 내 프로필 수정 요청 DTO
 * - 성별, 생년월일은 수정 불가 → 여기서도 아예 받지 않는다
 * - null 인 필드는 "변경하지 않음" 으로 처리
 */
data class ProfileUpdateRequest(
    val nickname: String? = null,
    val intro: String? = null,
    val region: String? = null,
    val job: String? = null,
    val avatarUrl: String? = null
)
