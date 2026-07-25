package com.chris.chipherlink.ai

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Manages privacy permissions for CipherAI.
 * Controls what data the AI can access.
 *
 * Privacy Rules:
 * - CipherAI must NEVER access messages, profile, or files without explicit permission
 * - User must grant permission for each data access type
 * - Permissions can be revoked at any time
 */
class AiPrivacyManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Check if AI has permission to access a specific data type.
     */
    fun hasPermission(permission: AiPermission): Boolean {
        return prefs.getBoolean(KEY_PREFIX + permission.key, false)
    }

    /**
     * Grant permission to AI for a specific data type.
     */
    fun grantPermission(permission: AiPermission) {
        prefs.edit().putBoolean(KEY_PREFIX + permission.key, true).apply()
    }

    /**
     * Revoke permission from AI for a specific data type.
     */
    fun revokePermission(permission: AiPermission) {
        prefs.edit().putBoolean(KEY_PREFIX + permission.key, false).apply()
    }

    /**
     * Revoke all AI permissions.
     */
    fun revokeAllPermissions() {
        prefs.edit().clear().apply()
    }

    /**
     * Get all current permission states.
     */
    fun getAllPermissions(): Map<AiPermission, Boolean> {
        return AiPermission.entries.associateWith { hasPermission(it) }
    }

    companion object {
        private const val PREFS_NAME = "cipherlink_ai_privacy"
        private const val KEY_PREFIX = "perm_"
    }
}

/**
 * Permissions that control AI access to user data.
 */
enum class AiPermission(val key: String, val displayName: String, val description: String) {
    ACCESS_MESSAGES("access_messages", "Chat Messages", "Allow AI to read your conversations"),
    ACCESS_PROFILE("access_profile", "Profile Data", "Allow AI to access your name and photo"),
    ACCESS_FILES("access_files", "Files", "Allow AI to access your files"),
    SEARCH_CHATS("search_chats", "Search Chats", "Allow AI to search through your messages"),
    SUMMARIZE_CHATS("summarize_chats", "Summarize Chats", "Allow AI to summarize conversations"),
    TRANSLATE_MESSAGES("translate_messages", "Translate", "Allow AI to translate messages")
}
