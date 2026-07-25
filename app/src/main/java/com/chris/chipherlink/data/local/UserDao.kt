package com.chris.chipherlink.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun usernameExists(username: String): Boolean

    @Query("SELECT * FROM users WHERE cipherLinkId = :cipherLinkId LIMIT 1")
    suspend fun getByCipherLinkId(cipherLinkId: String): UserEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE cipherLinkId = :cipherLinkId)")
    suspend fun cipherLinkIdExists(cipherLinkId: String): Boolean
}
