package demo.auth

import demo.auth.dto.AuthResponse
import demo.auth.dto.LoginRequest
import demo.auth.dto.RefreshRequest
import demo.auth.dto.SignupRequest
import demo.auth.dto.UserResponse
import demo.profile.Gender
import demo.profile.ProfileEntity
import demo.profile.ProfileRepository
import demo.saju.SajuMatchRequest
import demo.saju.SajuPersonPayload
import demo.saju.SajuPythonClient
import demo.saju.SajuTendencyUtil
import demo.user.UserEntity
import demo.user.UserRepository
import demo.user.UserStatus
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Service
class AuthServiceImpl(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
    private val sajuPythonClient: SajuPythonClient
) : AuthService {

    private val log = LoggerFactory.getLogger(AuthServiceImpl::class.java)

    @Transactional
    override fun signup(request: SignupRequest): AuthResponse {
        log.info(
            ">>> signup request: email={}, nickname={}, gender={}, birth={}",
            request.email,
            request.nickname,
            request.gender,
            request.birth
        )

        // 1) 이메일 중복 체크
        if (userRepository.existsByEmail(request.email)) {
            log.warn(">>> signup failed - email already in use: {}", request.email)
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "이미 사용 중인 이메일입니다."
            )
        }

        // 2) 생년월일 파싱
        val parsedBirthDate = try {
            LocalDate.parse(request.birth)   // yyyy-MM-dd
        } catch (e: DateTimeParseException) {
            log.warn(">>> signup failed - invalid birth format: email={}, birth={}", request.email, request.birth)
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "생년월일 형식은 yyyy-MM-dd 여야 합니다."
            )
        }

        // 3) 비밀번호 해시
        val encodedPwd = passwordEncoder.encode(request.pwd)

        // 4) UserEntity 생성/저장
        val userEntity = UserEntity().apply {
            email = request.email
            passwordHash = encodedPwd
            status = UserStatus.ACTIVE
        }
        val savedUser = userRepository.save(userEntity)
        log.info(">>> signup user saved: userId={}", savedUser.id)

        // 5) 자기 자신 vs 자기 자신으로 사주 엔진 호출 → 개인 성향 계산
        val genderInt = when (request.gender) {
            Gender.MALE -> 1
            Gender.FEMALE -> 0
            Gender.OTHER -> 1   // 필요시 규칙 수정
        }

        val selfPayload = SajuPersonPayload(
            year = parsedBirthDate.year,
            month = parsedBirthDate.monthValue,
            day = parsedBirthDate.dayOfMonth,
            gender = genderInt
        )

        val selfMatchResult = sajuPythonClient.match(
            SajuMatchRequest(
                person0 = selfPayload,
                person1 = selfPayload
            )
        )

        val tendencyList = SajuTendencyUtil.fromSal(selfMatchResult.sal0)
        val tendencyTextForProfile = tendencyList.joinToString(" / ")

        // 6) ProfileEntity 생성/저장
        val profile = ProfileEntity(
            user = savedUser,
            nickname = request.nickname,
            intro = null,
            gender = request.gender,
            birthDate = parsedBirthDate,
            region = null,
            job = request.job,
            avatarUrl = null,
            tendency = tendencyTextForProfile
        )
        profileRepository.save(profile)
        log.info(">>> signup profile saved: userId={}, nickname={}", savedUser.id, request.nickname)

        // 7) Access / Refresh 토큰 발급 + 응답 DTO 생성
        val accessToken = jwtTokenProvider.generateAccessToken(savedUser.id!!)
        val refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.id!!)

        val userResponse = UserResponse(
            id = savedUser.id!!,
            email = savedUser.email
        )

        log.info(">>> signup success: userId={}", savedUser.id)

        return AuthResponse(
            jwt = accessToken,
            refreshToken = refreshToken,
            user = userResponse
        )
    }

    @Transactional(readOnly = true)
    override fun login(request: LoginRequest): AuthResponse {
        log.info(">>> login request: email={}", request.email)

        val user = userRepository.findByEmail(request.email)
            ?: run {
                log.warn(">>> login failed - user not found: email={}", request.email)
                throw ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "이메일 또는 비밀번호가 올바르지 않습니다."
                )
            }

        // BCrypt로 비밀번호 비교
        if (!passwordEncoder.matches(request.pwd, user.passwordHash)) {
            log.warn(">>> login failed - wrong password: email={}", request.email)
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "이메일 또는 비밀번호가 올바르지 않습니다."
            )
        }

        val accessToken = jwtTokenProvider.generateAccessToken(user.id!!)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id!!)

        val userResponse = UserResponse(
            id = user.id!!,
            email = user.email
        )

        log.info(">>> login success: userId={}", user.id)

        return AuthResponse(
            jwt = accessToken,
            refreshToken = refreshToken,
            user = userResponse
        )
    }

    /**
     * Refresh 토큰으로 Access / Refresh 재발급
     */
    @Transactional(readOnly = true)
    override fun refresh(request: RefreshRequest): AuthResponse {
        log.info(">>> refresh token request")

        val userId = jwtTokenProvider.parseUserIdFromRefreshToken(request.refreshToken)
            ?: run {
                log.warn(">>> refresh failed - invalid refresh token")
                throw ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "리프레시 토큰이 유효하지 않습니다."
                )
            }

        val user = userRepository.findById(userId).orElseThrow {
            log.warn(">>> refresh failed - user not found, userId={}", userId)
            ResponseStatusException(HttpStatus.UNAUTHORIZED, "사용자를 찾을 수 없습니다.")
        }

        if (user.status != UserStatus.ACTIVE) {
            log.warn(">>> refresh failed - user not active, userId={}", userId)
            throw ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "비활성화된 계정입니다."
            )
        }

        val newAccessToken = jwtTokenProvider.generateAccessToken(user.id!!)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user.id!!) // 토큰 로테이션

        val userResponse = UserResponse(
            id = user.id!!,
            email = user.email
        )

        log.info(">>> refresh success: userId={}", user.id)

        return AuthResponse(
            jwt = newAccessToken,
            refreshToken = newRefreshToken,
            user = userResponse
        )
    }
}
