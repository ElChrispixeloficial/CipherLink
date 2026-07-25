package com.chris.chipherlink.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE localUserId = :userId AND isBlocked = 0 ORDER BY displayName ASC")
    fun getContactsByUserId(userId: String): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE localUserId = :userId AND isBlocked = 0 ORDER BY displayName ASC")
    suspend fun getContactsByUserIdList(userId: String): List<ContactEntity>

    @Query("SELECT * FROM contacts WHERE cipherLinkId = :cipherLinkId LIMIT 1")
    suspend fun getByCipherLinkId(cipherLinkId: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE localUserId = :userId AND cipherLinkId = :cipherLinkId LIMIT 1")
    suspend fun getByLocalUserAndCipherId(userId: String, cipherLinkId: String): ContactEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM contacts WHERE localUserId = :userId AND cipherLinkId = :cipherLinkId)")
    suspend fun isContact(userId: String, cipherLinkId: String): Boolean

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteById(contactId: String)

    @Query("DELETE FROM contacts WHERE localUserId = :userId")
    suspend fun deleteAllByUserId(userId: String)

    @Query("UPDATE contacts SET isBlocked = :blocked WHERE id = :contactId")
    suspend fun setBlocked(contactId: String, blocked: Boolean)

    @Query("UPDATE contacts SET displayName = :name WHERE id = :contactId")
    suspend fun updateDisplayName(contactId: String, name: String)
}
