package demo.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.Date

/**
 * JWT 발급 및 검증을 담당하는 Provider 클래스
 *
 * - Access / Refresh 두 종류의 토큰을 관리
 * - subject 에 userId 를 저장
 * - "type" 클레임으로 ACCESS / REFRESH 구분
 */
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

    /**
     * 서명에 사용할 알고리즘
     */
    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)

    /**
     * 토큰 검증용 Verifier
     */
    private val verifier: JWTVerifier
        get() = JWT.require(algorithm).build()

    // ---------------------------------------------------------------------
    // 토큰 생성부
    // ---------------------------------------------------------------------

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
     * 기존 코드 호환용:
     *  - generateToken() == Access Token 발급
     */
    fun generateToken(userId: Long): String =
        generateAccessToken(userId)

    // ---------------------------------------------------------------------
    // 토큰 검증 및 userId 추출부
    // ---------------------------------------------------------------------

    /**
     * 내부 공통 검증 로직
     *
     * @param token        검증할 토큰 문자열
     * @param requiredType 필요 시 요구하는 토큰 타입 (ACCESS / REFRESH), null 이면 타입 체크 안함
     * @return userId (Long) 또는 검증 실패 시 null
     */
    private fun verifyAndGetUserId(
        token: String,
        requiredType: JwtTokenType?
    ): Long? {
        return try {
            val decoded = verifier.verify(token)

            // type 클레임 검사 (requiredType 이 지정된 경우)
            if (requiredType != null) {
                val typeClaim = decoded.getClaim("type")?.asString()
                if (typeClaim == null || typeClaim != requiredType.name) {
                    // 타입이 일치하지 않으면 null
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
     * (Authorization 헤더 파싱 등에 사용 가능)
     */
    fun parseUserId(token: String): Long? =
        verifyAndGetUserId(token, null)

    /**
     * Refresh 토큰에서만 userId 추출
     */
    fun parseUserIdFromRefreshToken(refreshToken: String): Long? =
        verifyAndGetUserId(refreshToken, JwtTokenType.REFRESH)

    /**
     * Access 토큰에서만 userId 추출
     */
    fun parseUserIdFromAccessToken(accessToken: String): Long? =
        verifyAndGetUserId(accessToken, JwtTokenType.ACCESS)

    /**
     * ✅ 기존 코드 호환용 메서드
     * 예전 컨트롤러에서 getUserIdFromToken(token) 을 쓰고 있었다면,
     * 이 메서드를 통해 그대로 동작하도록 유지할 수 있습니다.
     */
    fun getUserIdFromToken(token: String): Long? =
        parseUserId(token)
}
