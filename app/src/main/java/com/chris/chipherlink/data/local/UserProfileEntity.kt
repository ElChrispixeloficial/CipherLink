package com.chris.chipherlink.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey val userId: String,
    val displayName: String,
    val email: String?,
    val photoPath: String?,
    val createdAt: Long
)
