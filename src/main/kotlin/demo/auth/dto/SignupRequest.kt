package demo.auth.dto

import demo.profile.Gender

data class SignupRequest(
    val email: String,
    val pwd: String,
    val nickname: String,
    val birth: String,     //  "2001-01-01"
    val gender: Gender,     // "MALE" / "FEMALE" / "OTHER"
    val job: String
)
