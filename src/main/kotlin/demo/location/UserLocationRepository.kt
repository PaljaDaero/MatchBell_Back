    package demo.location

    import demo.user.UserEntity
    import org.springframework.data.jpa.repository.JpaRepository

    interface UserLocationRepository : JpaRepository<UserLocationEntity, Long> {

        fun findByUser(user: UserEntity): UserLocationEntity?

        fun findByUserId(userId: Long): UserLocationEntity?
    }
