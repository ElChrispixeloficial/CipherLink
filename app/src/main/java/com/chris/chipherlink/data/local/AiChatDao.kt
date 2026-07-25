package com.chris.chipherlink.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chat: AiChatEntity)

    @Query("SELECT * FROM ai_chats ORDER BY lastMessageAt DESC")
    fun getAll(): Flow<List<AiChatEntity>>

    @Query("SELECT * FROM ai_chats WHERE id = :chatId")
    suspend fun getById(chatId: String): AiChatEntity?

    @Query("UPDATE ai_chats SET lastMessageAt = :timestamp WHERE id = :chatId")
    suspend fun updateLastMessage(chatId: String, timestamp: Long)

    @Query("DELETE FROM ai_chats WHERE id = :chatId")
    suspend fun deleteById(chatId: String)

    @Query("DELETE FROM ai_chats")
    suspend fun deleteAll()
}
