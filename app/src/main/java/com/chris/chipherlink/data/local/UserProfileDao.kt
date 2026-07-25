package com.chris.chipherlink.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: UserProfileEntity): Long

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getById(userId: String): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE userId = :userId LIMIT 1")
    fun observeById(userId: String): Flow<UserProfileEntity?>

    @Query("UPDATE user_profiles SET displayName = :name WHERE userId = :userId")
    suspend fun updateDisplayName(userId: String, name: String)

    @Query("UPDATE user_profiles SET photoPath = :path WHERE userId = :userId")
    suspend fun updatePhotoPath(userId: String, path: String?)
}
