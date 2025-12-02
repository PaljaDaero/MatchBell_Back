package demo.auth.dto

data class AuthResponse(
    val jwt: String,
    val user: UserResponse
)
