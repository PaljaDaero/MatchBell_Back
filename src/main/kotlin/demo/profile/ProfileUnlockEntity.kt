package demo.profile

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "profile_unlocks",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_profile_unlock_viewer_target",
            columnNames = ["viewer_id", "target_id"]
        )
    ]
)
class ProfileUnlockEntity() {   // JPA 용 기본 생성자

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    /**
     *  관람자 사용자 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "viewer_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_profile_unlock_viewer")
    )
    lateinit var viewer: UserEntity

    /**
     *  대상자 사용자 정보
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "target_id",
        nullable = false,
        foreignKey = ForeignKey(name = "fk_profile_unlock_target")
    )
    lateinit var target: UserEntity

    @Column(name = "created_at", nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()

    //  편의 생성자
    constructor(
        viewer: UserEntity,
        target: UserEntity,
        createdAt: LocalDateTime = LocalDateTime.now()
    ) : this() {
        this.viewer = viewer
        this.target = target
        this.createdAt = createdAt
    }
}
