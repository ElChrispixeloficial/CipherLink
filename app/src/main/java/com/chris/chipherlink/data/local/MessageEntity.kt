package com.chris.chipherlink.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Message delivery status for the beta messaging system.
 */
enum class DeliveryStatus {
    SENDING,    // Message is being sent
    SENT,       // Message sent to server/peer
    DELIVERED,  // Message received by peer
    READ,       // Message read by peer
    FAILED      // Send failed
}

/**
 * Message content type.
 */
enum class MessageType {
    TEXT,       // Plain text message
    IMAGE,      // Image message (future)
    FILE,       // File attachment (future)
    SYSTEM      // System message (e.g., "user joined")
}

@Entity(
    tableName = "messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatEntity::class,
            parentColumns = ["id"],
            childColumns = ["chatId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["chatId"])]
)
data class MessageEntity(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val content: String,
    val timestamp: Long,
    val isSentByMe: Boolean,
    val isRead: Boolean,
    val deliveryStatus: String = DeliveryStatus.SENT.name,
    val messageType: String = MessageType.TEXT.name
)
