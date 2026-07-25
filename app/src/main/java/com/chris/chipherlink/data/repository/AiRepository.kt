package com.chris.chipherlink.data.repository

import com.chris.chipherlink.data.local.AiChatDao
import com.chris.chipherlink.data.local.AiChatEntity
import com.chris.chipherlink.data.local.AiMessageDao
import com.chris.chipherlink.data.local.AiMessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Repository for AI chat and message operations.
 */
class AiRepository(
    private val aiChatDao: AiChatDao,
    private val aiMessageDao: AiMessageDao
) {
    /** Observe all AI chats. */
    fun getAllChats(): Flow<List<AiChatEntity>> = aiChatDao.getAll()

    /** Get a chat by ID. */
    suspend fun getChatById(chatId: String): AiChatEntity? = aiChatDao.getById(chatId)

    /** Observe messages for an AI chat. */
    fun getMessages(chatId: String): Flow<List<AiMessageEntity>> = aiMessageDao.getByChatId(chatId)

    /** Get messages as a list (for sending to AI provider). */
    suspend fun getMessagesList(chatId: String): List<AiMessageEntity> = aiMessageDao.getByChatIdList(chatId)

    /** Create a new AI chat. */
    suspend fun createChat(title: String, mode: String = "general"): String {
        val chatId = UUID.randomUUID().toString()
        val chat = AiChatEntity(
            id = chatId,
            title = title,
            createdAt = System.currentTimeMillis(),
            lastMessageAt = System.currentTimeMillis(),
            mode = mode
        )
        aiChatDao.insert(chat)
        return chatId
    }

    /** Send a user message. */
    suspend fun sendUserMessage(chatId: String, content: String): String {
        val messageId = UUID.randomUUID().toString()
        val message = AiMessageEntity(
            id = messageId,
            chatId = chatId,
            role = "user",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        aiMessageDao.insert(message)
        aiChatDao.updateLastMessage(chatId, System.currentTimeMillis())
        return messageId
    }

    /** Save an assistant message. */
    suspend fun saveAssistantMessage(chatId: String, content: String): String {
        val messageId = UUID.randomUUID().toString()
        val message = AiMessageEntity(
            id = messageId,
            chatId = chatId,
            role = "assistant",
            content = content,
            timestamp = System.currentTimeMillis()
        )
        aiMessageDao.insert(message)
        aiChatDao.updateLastMessage(chatId, System.currentTimeMillis())
        return messageId
    }

    /** Delete an AI chat and its messages. */
    suspend fun deleteChat(chatId: String) {
        aiChatDao.deleteById(chatId)
    }

    /** Delete all AI data. */
    suspend fun deleteAll() {
        aiChatDao.deleteAll()
    }
}
