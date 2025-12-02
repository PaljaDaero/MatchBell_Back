package demo.congif

import demo.user.UserEntity
import demo.user.UserRepository
import demo.user.UserStatus
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class InitConfig(
    private val userRepository: UserRepository
) {

    // 초기 데이터 삽입
    @Bean
    fun initData(): CommandLineRunner = CommandLineRunner {
        
        if (!userRepository.existsByEmail("test1@example.com")) {
            val user = UserEntity().apply {
                email = "test1@example.com"
                passwordHash = "1234"    
                status = UserStatus.ACTIVE
            }
            userRepository.save(user)
            println("[InitConfig] created default user test1@example.com / 1234")
        }

       
    }
}
