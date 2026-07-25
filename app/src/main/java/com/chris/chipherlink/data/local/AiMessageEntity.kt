package com.chris.chipherlink.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ai_messages",
    foreignKeys = [
        ForeignKey(
            entity = AiChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("chatId")]
)
data class AiMessageEntity(
    @PrimaryKey
    val id: String,
    val chatId: String,
    val role: String, // "user", "assistant", "system"
    val content: String,
    val timestamp: Long
)
