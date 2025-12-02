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

    @Value("\${jwt.expiration-seconds:3600}")
    private val expirationSeconds: Long
) {

    private val algorithm: Algorithm
        get() = Algorithm.HMAC256(secret)

    private val verifier: JWTVerifier
        get() = JWT.require(algorithm).build()

    /* userId -> JWT */
    fun generateToken(userId: Long): String {
        val now = Instant.now()
        val expiresAt = now.plusSeconds(expirationSeconds)

        return JWT.create()
            .withSubject(userId.toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(expiresAt))
            .sign(algorithm)
    }

    /* token -> userId */
    fun parseUserId(token: String): Long? =
        try {
            val decoded = verifier.verify(token)
            decoded.subject.toLongOrNull()
        } catch (ex: JWTVerificationException) {
            null
        }
}
