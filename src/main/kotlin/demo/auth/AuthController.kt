package demo.auth

import demo.auth.dto.AuthResponse
import demo.auth.dto.LoginRequest
import demo.auth.dto.SignupRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {

    @PostMapping("/signup")
    fun signup(
        @RequestBody req: SignupRequest
    ): ResponseEntity<AuthResponse> {
        val result = authService.signup(req)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(result)
    }

    @PostMapping("/login")
    fun login(
        @RequestBody req: LoginRequest
    ): ResponseEntity<AuthResponse> {
        val result = authService.login(req)
        return ResponseEntity.ok(result)
    }
}
