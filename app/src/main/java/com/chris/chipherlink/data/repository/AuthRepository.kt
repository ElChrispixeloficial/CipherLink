package com.chris.chipherlink.data.repository

import com.chris.chipherlink.data.local.SessionDao
import com.chris.chipherlink.data.local.SessionEntity
import com.chris.chipherlink.data.local.UserDao
import com.chris.chipherlink.data.local.UserEntity
import com.chris.chipherlink.data.local.SessionManager
import com.chris.chipherlink.integrity.IdentityManager
import com.chris.chipherlink.integrity.IntegrityManager
import com.chris.chipherlink.utils.CipherLinkIdGenerator
import com.chris.chipherlink.utils.PasswordUtils
import java.util.UUID

/** Handles authentication: registration, login, session management. */
class AuthRepository(
    private val userDao: UserDao,
    private val sessionDao: SessionDao,
    private val sessionManager: SessionManager,
    private val identityManager: IdentityManager,
    private val integrityManager: IntegrityManager,
    private val profileRepository: ProfileRepository
) {
    /** Register a new user. Creates profile, identity, and integrity fingerprints. */
    suspend fun register(
        username: String,
        email: String?,
        password: String
    ): Result<String> {
        return try {
            if (userDao.usernameExists(username)) {
                return Result.failure(IllegalArgumentException("Username already taken"))
            }

            val salt = PasswordUtils.generateSalt()
            val passwordHash = PasswordUtils.hashPassword(password, salt)
            val userId = UUID.randomUUID().toString()

            // Generate unique CipherLink ID
            var cipherLinkId: String
            do {
                cipherLinkId = CipherLinkIdGenerator.generate()
            } while (userDao.cipherLinkIdExists(cipherLinkId))

            val user = UserEntity(
                id = userId,
                username = username,
                email = email,
                passwordHash = "$salt:$passwordHash",
                cipherLinkId = cipherLinkId,
                createdAt = System.currentTimeMillis()
            )
            userDao.insert(user)
            createSession(userId, username)

            // v0.4: Create user profile
            profileRepository.createProfile(userId, username, email)

            // Generate identity and integrity fingerprints
            if (!identityManager.hasIdentity()) {
                identityManager.generateIdentity()
            }
            integrityManager.generateFingerprints()

            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Login an existing user. */
    suspend fun login(username: String, password: String): Result<String> {
        return try {
            val user = userDao.getByUsername(username)
                ?: return Result.failure(IllegalArgumentException("User not found"))

            val parts = user.passwordHash.split(":")
            if (parts.size != 2) return Result.failure(Exception("Invalid password data"))

            val salt = parts[0]
            val storedHash = parts[1]

            if (!PasswordUtils.verifyPassword(password, storedHash, salt)) {
                return Result.failure(IllegalArgumentException("Incorrect password"))
            }

            createSession(user.id, user.username)

            // Ensure identity exists and update fingerprints
            if (!identityManager.hasIdentity()) {
                identityManager.generateIdentity()
            }
            integrityManager.generateFingerprints()

            Result.success(user.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /** Get the currently logged in user's ID, or null. */
    fun getCurrentUserId(): String? = sessionManager.getCurrentUserId()

    /** Check if a session is active. */
    fun hasActiveSession(): Boolean = sessionManager.hasActiveSession()

    /** Logout: clear session and integrity data. */
    suspend fun logout() {
        sessionDao.clearAll()
        sessionManager.clearSession()
        integrityManager.clearFingerprints()
    }

    private suspend fun createSession(userId: String, username: String) {
        val session = SessionEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            username = username,
            createdAt = System.currentTimeMillis()
        )
        sessionDao.insert(session)
        sessionManager.saveSession(userId, username)
    }
}
