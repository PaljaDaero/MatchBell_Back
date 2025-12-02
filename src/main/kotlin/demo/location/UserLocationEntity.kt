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
data class UserLocationEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "user_id",
        nullable = false,
        unique = true,
        foreignKey = ForeignKey(name = "fk_location_user")
    )
    val user: UserEntity,

    @Column(name = "lat", nullable = false)
    val lat: Double,

    @Column(name = "lng", nullable = false)
    val lng: Double,

    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
