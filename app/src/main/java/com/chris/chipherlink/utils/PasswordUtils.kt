package com.chris.chipherlink.utils

import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Utility for hashing passwords using SHA-256 with salt.
 * Not for production E2EE — just for local user auth.
 */
object PasswordUtils {

    private const val SALT_LENGTH = 16

    /** Generates a random salt as a hex string. */
    fun generateSalt(): String {
        val salt = ByteArray(SALT_LENGTH)
        SecureRandom().nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    /** Hashes a password with the given salt using SHA-256. */
    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt.toByteArray(Charsets.UTF_8))
        val digest = md.digest(password.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    /** Verifies a password against a stored hash and salt. */
    fun verifyPassword(password: String, storedHash: String, salt: String): Boolean {
        return hashPassword(password, salt) == storedHash
    }
}
