package demo.auth

import demo.auth.dto.AuthResponse
import demo.auth.dto.LoginRequest
import demo.auth.dto.RefreshRequest
import demo.auth.dto.SignupRequest

interface AuthService {
    fun signup(request: SignupRequest): AuthResponse
    fun login(request: LoginRequest): AuthResponse

    // 새로 추가
    fun refresh(request: RefreshRequest): AuthResponse
}
