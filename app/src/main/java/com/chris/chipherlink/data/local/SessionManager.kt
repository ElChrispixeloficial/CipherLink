package com.chris.chipherlink.data.local

import android.content.Context
import android.content.SharedPreferences
import java.util.UUID

/**
 * Manages the active user session via SharedPreferences.
 * Provides synchronous access for splash screen routing.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    /** Check if a user session is active. */
    fun hasActiveSession(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    /** Get the currently logged in user's ID, or null. */
    fun getCurrentUserId(): String? {
        return if (hasActiveSession()) prefs.getString(KEY_USER_ID, null) else null
    }

    /** Get the currently logged in username, or null. */
    fun getCurrentUsername(): String? {
        return if (hasActiveSession()) prefs.getString(KEY_USERNAME, null) else null
    }

    /** Save a new session after login/register. */
    fun saveSession(userId: String, username: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USERNAME, username)
            .apply()
    }

    /** Clear session on logout. */
    fun clearSession() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "cipherlink_session"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USERNAME = "username"
    }
}
