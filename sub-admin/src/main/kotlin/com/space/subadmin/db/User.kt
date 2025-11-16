package com.space.subadmin.db

import com.space.subadmin.db.Role
import com.space.subadmin.users.SnowflakeIdSequence
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.GenericGenerator
import java.util.UUID

@Entity
@Table(name = "users")
data class User(
    @Id
    @SnowflakeIdSequence
    val id:  Long = 1,

    @Column(name ="uuid")
    val uuid: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true)
    var username: String = "",

    @Column(nullable = false)
    var passwordHash: String = "",

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: Role = Role.STAFF
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        // Use ID for equality if it's not the default value, otherwise it's a new entity
        return id != 1L && id == other.id
    }

    override fun hashCode(): Int {
        // Consistent with the equals implementation
        return if (id != 1L) id.hashCode() else super.hashCode()
    }

    override fun toString(): String {
        // Leverage the data class's default toString() for clarity
        return "User(id=$id, uuid=$uuid, username='$username', role=$role)"
    }
}