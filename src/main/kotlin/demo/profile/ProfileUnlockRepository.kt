package demo.profile

import org.springframework.data.jpa.repository.JpaRepository

interface ProfileUnlockRepository : JpaRepository<ProfileUnlockEntity, Long> {

    fun existsByViewerIdAndTargetId(viewerId: Long, targetId: Long): Boolean
}
