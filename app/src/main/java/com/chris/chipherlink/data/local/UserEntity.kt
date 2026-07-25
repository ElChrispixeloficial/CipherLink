package com.chris.chipherlink.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["username"], unique = true),
        Index(value = ["cipherLinkId"], unique = true)
    ]
)
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String?,
    val passwordHash: String,
    val cipherLinkId: String,
    val createdAt: Long
)
