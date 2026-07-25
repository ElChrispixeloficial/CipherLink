package com.chris.chipherlink.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_chats")
data class AiChatEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val createdAt: Long,
    val lastMessageAt: Long,
    val mode: String = "general" // "general" or "assistant"
)
