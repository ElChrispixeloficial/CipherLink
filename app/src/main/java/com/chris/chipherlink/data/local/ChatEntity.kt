package com.chris.chipherlink.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdBy: String,
    val createdAt: Long,
    val lastMessage: String?,
    val lastMessageTimestamp: Long
)
