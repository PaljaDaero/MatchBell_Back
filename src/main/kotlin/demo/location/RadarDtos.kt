package demo.location

import demo.profile.Gender

data class RadarMeDto(
    val lat: Double,
    val lng: Double,
    val region: String?
)

data class RadarUserDto(
    val userId: Long,
    val nickname: String,
    val gender: Gender,
    val age: Int?,
    val distanceMeters: Double,
    val region: String?,
    val avatarUrl: String?,

    // Python 궁합 지표
    val originalScore: Double,
    val finalScore: Double,
    val stressScore: Double,

    val tendency0: List<String>,
    val tendency1: List<String>
)


data class RadarResponse(
    val me: RadarMeDto,
    val users: List<RadarUserDto>
)
