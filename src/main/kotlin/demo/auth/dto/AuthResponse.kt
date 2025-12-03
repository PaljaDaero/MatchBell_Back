package demo.auth.dto

data class AuthResponse(
    val jwt: String,          // Access Token
    val refreshToken: String, // Refresh Token
    val user: UserResponse
)
