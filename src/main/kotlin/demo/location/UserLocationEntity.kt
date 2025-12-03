package demo.location

import demo.user.UserEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "user_locations",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_location_user",
            columnNames = ["user_id"]
        )
    ]
)
class UserLocationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_location_user")
    )
    lateinit var user: UserEntity    // 사용자 위치 정보 소유자

    @Column(name = "lat", nullable = false)
    var lat: Double = 0.0

    @Column(name = "lng", nullable = false)
    var lng: Double = 0.0

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()

    // JPA 용 기본 생성자
    constructor()

    // 편의 생성자
    constructor(user: UserEntity, lat: Double, lng: Double) {
        this.user = user
        this.lat = lat
        this.lng = lng
        this.updatedAt = LocalDateTime.now()
    }
}
