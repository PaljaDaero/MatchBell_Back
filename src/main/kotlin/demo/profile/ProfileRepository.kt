package demo.profile

import demo.user.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ProfileRepository : JpaRepository<ProfileEntity, Long> {

    fun findByUser(user: UserEntity): ProfileEntity?

    fun findByUserId(userId: Long): ProfileEntity?

    fun findByRegion(region: String): List<ProfileEntity>
}
