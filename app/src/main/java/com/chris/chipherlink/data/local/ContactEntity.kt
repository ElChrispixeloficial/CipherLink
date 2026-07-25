package com.chris.chipherlink.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a saved contact (another user's profile locally cached).
 */
@Entity(
    tableName = "contacts",
    indices = [
        Index(value = ["cipherLinkId"], unique = true),
        Index(value = ["localUserId"])
    ]
)
data class ContactEntity(
    @PrimaryKey val id: String,
    val localUserId: String,
    val cipherLinkId: String,
    val displayName: String,
    val username: String,
    val photoPath: String?,
    val addedAt: Long,
    val isBlocked: Boolean = false
)
