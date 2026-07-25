package com.chris.chipherlink.data.repository

import com.chris.chipherlink.data.local.UserProfileDao
import com.chris.chipherlink.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow

/** Manages user profile data: display name, photo, preferences. */
class ProfileRepository(private val profileDao: UserProfileDao) {

    /** Observe profile changes reactively. */
    fun observeProfile(userId: String): Flow<UserProfileEntity?> {
        return profileDao.observeById(userId)
    }

    /** Get profile synchronously. */
    suspend fun getProfile(userId: String): UserProfileEntity? {
        return profileDao.getById(userId)
    }

    /** Create or update profile on registration. */
    suspend fun createProfile(userId: String, username: String, email: String?) {
        val profile = UserProfileEntity(
            userId = userId,
            displayName = username,
            email = email,
            photoPath = null,
            createdAt = System.currentTimeMillis()
        )
        profileDao.insert(profile)
    }

    /** Update display name. */
    suspend fun updateDisplayName(userId: String, name: String) {
        profileDao.updateDisplayName(userId, name)
    }

    /** Update profile photo path. */
    suspend fun updatePhotoPath(userId: String, path: String?) {
        profileDao.updatePhotoPath(userId, path)
    }
}
