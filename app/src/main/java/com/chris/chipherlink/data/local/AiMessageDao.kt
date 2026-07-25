package com.chris.chipherlink.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AiMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(message: AiMessageEntity)

    @Query("SELECT * FROM ai_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getByChatId(chatId: String): Flow<List<AiMessageEntity>>

    @Query("SELECT * FROM ai_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    suspend fun getByChatIdList(chatId: String): List<AiMessageEntity>

    @Query("DELETE FROM ai_messages WHERE chatId = :chatId")
    suspend fun deleteByChatId(chatId: String)
}
