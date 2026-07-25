package com.chris.chipherlink.data.repository

import com.chris.chipherlink.data.local.ContactDao
import com.chris.chipherlink.data.local.ContactEntity
import com.chris.chipherlink.data.local.UserDao
import com.chris.chipherlink.utils.CipherLinkIdGenerator
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * Manages the local contacts directory.
 * Contacts are cached user profiles of people you've chatted with.
 */
class ContactRepository(
    private val contactDao: ContactDao,
    private val userDao: UserDao
) {
    fun getContacts(userId: String): Flow<List<ContactEntity>> {
        return contactDao.getContactsByUserId(userId)
    }

    suspend fun getContactsList(userId: String): List<ContactEntity> {
        return contactDao.getContactsByUserIdList(userId)
    }

    /**
     * Find a user by their CipherLink ID (local database lookup).
     */
    suspend fun findByCipherLinkId(cipherLinkId: String): ContactEntity? {
        val normalized = CipherLinkIdGenerator.normalize(cipherLinkId)
        return contactDao.getByCipherLinkId(normalized)
    }

    /**
     * Find a user by their CipherLink ID in the users table (registration lookup).
     */
    suspend fun findUserByCipherLinkId(cipherLinkId: String): com.chris.chipherlink.data.local.UserEntity? {
        val normalized = CipherLinkIdGenerator.normalize(cipherLinkId)
        return userDao.getByCipherLinkId(normalized)
    }

    /**
     * Check if a CipherLink ID belongs to the current user.
     */
    suspend fun isOwnId(userId: String, cipherLinkId: String): Boolean {
        val user = userDao.getById(userId)
        return user?.cipherLinkId == CipherLinkIdGenerator.normalize(cipherLinkId)
    }

    /**
     * Add a contact from a found user.
     */
    suspend fun addContact(
        localUserId: String,
        targetUserId: String,
        cipherLinkId: String,
        displayName: String,
        username: String,
        photoPath: String? = null
    ): ContactEntity {
        val existing = contactDao.getByLocalUserAndCipherId(localUserId, cipherLinkId)
        if (existing != null) return existing

        val contact = ContactEntity(
            id = UUID.randomUUID().toString(),
            localUserId = localUserId,
            cipherLinkId = CipherLinkIdGenerator.normalize(cipherLinkId),
            displayName = displayName,
            username = username,
            photoPath = photoPath,
            addedAt = System.currentTimeMillis()
        )
        contactDao.insert(contact)
        return contact
    }

    suspend fun isContact(userId: String, cipherLinkId: String): Boolean {
        return contactDao.isContact(userId, CipherLinkIdGenerator.normalize(cipherLinkId))
    }

    suspend fun removeContact(contactId: String) {
        contactDao.deleteById(contactId)
    }

    suspend fun blockContact(contactId: String) {
        contactDao.setBlocked(contactId, true)
    }

    suspend fun unblockContact(contactId: String) {
        contactDao.setBlocked(contactId, false)
    }

    suspend fun updateDisplayName(contactId: String, name: String) {
        contactDao.updateDisplayName(contactId, name)
    }

    suspend fun deleteAll(userId: String) {
        contactDao.deleteAllByUserId(userId)
    }
}
