package com.chris.chipherlink.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SessionEntity): Long

    @Query("SELECT * FROM sessions LIMIT 1")
    suspend fun getActiveSession(): SessionEntity?

    @Query("DELETE FROM sessions")
    suspend fun clearAll()
}
