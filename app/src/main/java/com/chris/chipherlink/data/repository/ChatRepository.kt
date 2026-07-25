package com.chris.chipherlink.data.repository

import com.chris.chipherlink.data.local.ChatDao
import com.chris.chipherlink.data.local.ChatEntity
import com.chris.chipherlink.data.local.DeliveryStatus
import com.chris.chipherlink.data.local.MessageDao
import com.chris.chipherlink.data.local.MessageEntity
import com.chris.chipherlink.data.local.MessageType
import com.chris.chipherlink.integrity.IntegrityManager
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Handles chat and message operations against Room.
 * v0.5: Added delivery status tracking and message types.
 */
class ChatRepository(
    private val chatDao: ChatDao,
    private val messageDao: MessageDao,
    private val integrityManager: IntegrityManager
) {
    /** Observe all chats for a user. */
    fun getChatsByUserId(userId: String): Flow<List<ChatEntity>> {
        return chatDao.getByUserId(userId)
    }

    /** Observe messages for a chat. */
    fun getMessagesByChatId(chatId: String): Flow<List<MessageEntity>> {
        return messageDao.getByChatId(chatId)
    }

    /** Get unread message count for a chat. */
    fun getUnreadCount(chatId: String): Flow<Int> {
        return messageDao.getUnreadCount(chatId)
    }

    /** Create a new chat conversation. */
    suspend fun createChat(name: String, createdBy: String): Result<String> {
        return try {
            val chatId = UUID.randomUUID().toString()
            val chat = ChatEntity(
                id = chatId,
                name = name,
                createdBy = createdBy,
                createdAt = System.currentTimeMillis(),
                lastMessage = null,
                lastMessageTimestamp = System.currentTimeMillis()
            )
            chatDao.insert(chat)
            integrityManager.generateFingerprints()
            Result.success(chatId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Create a chat with a contact (by their user ID). */
    suspend fun createChatWithContact(
        name: String,
        createdBy: String,
        contactUserId: String
    ): Result<String> {
        return createChat(name, createdBy)
    }

    /** Send a text message with delivery tracking. */
    suspend fun sendMessage(
        chatId: String,
        senderId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT
    ): Result<String> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val message = MessageEntity(
                id = messageId,
                chatId = chatId,
                senderId = senderId,
                content = content,
                timestamp = System.currentTimeMillis(),
                isSentByMe = true,
                isRead = false,
                deliveryStatus = DeliveryStatus.SENDING.name,
                messageType = messageType.name
            )
            messageDao.insert(message)

            chatDao.updateLastMessage(
                chatId = chatId,
                message = content,
                timestamp = System.currentTimeMillis()
            )

            integrityManager.generateFingerprints()

            // Simulate delivery status progression
            // In a real implementation, this would be driven by network callbacks
            messageDao.updateDeliveryStatus(messageId, DeliveryStatus.SENT.name)

            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Receive a message from another user (simulated for local beta). */
    suspend fun receiveMessage(
        chatId: String,
        senderId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT
    ): Result<String> {
        return try {
            val messageId = UUID.randomUUID().toString()
            val message = MessageEntity(
                id = messageId,
                chatId = chatId,
                senderId = senderId,
                content = content,
                timestamp = System.currentTimeMillis(),
                isSentByMe = false,
                isRead = false,
                deliveryStatus = DeliveryStatus.DELIVERED.name,
                messageType = messageType.name
            )
            messageDao.insert(message)

            chatDao.updateLastMessage(
                chatId = chatId,
                message = content,
                timestamp = System.currentTimeMillis()
            )

            integrityManager.generateFingerprints()
            Result.success(messageId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Mark all messages in a chat as read. */
    suspend fun markAsRead(chatId: String) {
        messageDao.markAllAsRead(chatId)
    }

    /** Update message delivery status. */
    suspend fun updateDeliveryStatus(messageId: String, status: DeliveryStatus) {
        messageDao.updateDeliveryStatus(messageId, status.name)
    }

    /** Get a chat by ID. */
    suspend fun getChatById(chatId: String): ChatEntity? {
        return chatDao.getById(chatId)
    }

    /** Delete a chat. */
    suspend fun deleteChat(chatId: String) {
        chatDao.deleteById(chatId)
        integrityManager.generateFingerprints()
    }
}
