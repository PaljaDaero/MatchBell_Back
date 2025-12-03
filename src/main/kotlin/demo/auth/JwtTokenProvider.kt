package demo.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date


@Component
class JwtTokenProvider(

    @Value("\${jwt.secret}")
    private val secret: String,

    // Access 토큰 만료 시간 (초)
    @Value("\${jwt.access-expiration-seconds:3600}")
    private val accessExpirationSeconds: Long,

    // Refresh 토큰 만료 시간 (초)
    @Value("\${jwt.refresh-expiration-seconds:1209600}")
    private val refreshExpirationSeconds: Long
) {

    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)

    private val verifier: JWTVerifier
        get() = JWT.require(algorithm).build()

    /**
     * 내부용 공통 생성 함수
     */
    private fun generateTokenInternal(
        userId: Long,
        type: JwtTokenType,
        expirationSeconds: Long
    ): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(expirationSeconds)

        return JWT.create()
            .withSubject(userId.toString())
            .withClaim("type", type.name)   // ACCESS / REFRESH 구분
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }

    /**
     * Access Token 발급
     */
    fun generateAccessToken(userId: Long): String =
        generateTokenInternal(userId, JwtTokenType.ACCESS, accessExpirationSeconds)

    /**
     * Refresh Token 발급
     */
    fun generateRefreshToken(userId: Long): String =
        generateTokenInternal(userId, JwtTokenType.REFRESH, refreshExpirationSeconds)

    /**
     * 기존 코드 호환용: generateToken = Access Token
     */
    fun generateToken(userId: Long): String =
        generateAccessToken(userId)

    /**
     * 내부 공통 검증 로직
     */
    private fun verifyAndGetUserId(
        token: String,
        requiredType: JwtTokenType?
    ): Long? {
        return try {
            val decoded = verifier.verify(token)

            if (requiredType != null) {
                val typeClaim = decoded.getClaim("type")?.asString()
                if (typeClaim == null || typeClaim != requiredType.name) {
                    return null
                }
            }

            decoded.subject.toLongOrNull()
        } catch (ex: JWTVerificationException) {
            null
        }
    }

    /**
     * 기존 코드에서 쓰던 것: 타입 체크 없이 userId 만 뽑기
     * (Authorization 헤더 파싱 등에 사용)
     */
    fun parseUserId(token: String): Long? =
        verifyAndGetUserId(token, null)

    /**
     * Refresh 토큰에서만 userId 추출
     */
    fun parseUserIdFromRefreshToken(refreshToken: String): Long? =
        verifyAndGetUserId(refreshToken, JwtTokenType.REFRESH)

    /**
     * Access 토큰에서만 userId 추출 (필요하면 사용)
     */
    fun parseUserIdFromAccessToken(accessToken: String): Long? =
        verifyAndGetUserId(accessToken, JwtTokenType.ACCESS)
}
