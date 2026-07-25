package com.chris.chipherlink.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages encrypted SharedPreferences for sensitive data.
 * Uses AES-256-GCM via Android Keystore.
 * All properties are reactive via StateFlow for Compose collection.
 */
class SecurePreferences(context: Context) {

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

    private val _themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, THEME_SYSTEM) ?: THEME_SYSTEM)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _accentColor = MutableStateFlow(prefs.getString(KEY_ACCENT_COLOR, "teal") ?: "teal")
    val accentColor: StateFlow<String> = _accentColor.asStateFlow()

    private val _chatBackground = MutableStateFlow(prefs.getString(KEY_CHAT_BG, "default") ?: "default")
    val chatBackground: StateFlow<String> = _chatBackground.asStateFlow()

    private val _animationsEnabled = MutableStateFlow(prefs.getBoolean(KEY_ANIMATIONS, true))
    val animationsEnabled: StateFlow<Boolean> = _animationsEnabled.asStateFlow()

    private val _lastIntegrityCheck = MutableStateFlow(prefs.getLong(KEY_LAST_INTEGRITY, 0L))
    val lastIntegrityCheck: StateFlow<Long> = _lastIntegrityCheck.asStateFlow()

    private val _profilePhotoUri = MutableStateFlow(prefs.getString(KEY_PHOTO_URI, null))
    val profilePhotoUri: StateFlow<String?> = _profilePhotoUri.asStateFlow()

    var themeModeValue: String
        get() = _themeMode.value
        set(value) {
            prefs.edit().putString(KEY_THEME_MODE, value).apply()
            _themeMode.value = value
        }

    var accentColorValue: String
        get() = _accentColor.value
        set(value) {
            prefs.edit().putString(KEY_ACCENT_COLOR, value).apply()
            _accentColor.value = value
        }

    var chatBackgroundValue: String
        get() = _chatBackground.value
        set(value) {
            prefs.edit().putString(KEY_CHAT_BG, value).apply()
            _chatBackground.value = value
        }

    var animationsEnabledValue: Boolean
        get() = _animationsEnabled.value
        set(value) {
            prefs.edit().putBoolean(KEY_ANIMATIONS, value).apply()
            _animationsEnabled.value = value
        }

    var lastIntegrityCheckValue: Long
        get() = _lastIntegrityCheck.value
        set(value) {
            prefs.edit().putLong(KEY_LAST_INTEGRITY, value).apply()
            _lastIntegrityCheck.value = value
        }

    var profilePhotoUriValue: String?
        get() = _profilePhotoUri.value
        set(value) {
            prefs.edit().putString(KEY_PHOTO_URI, value).apply()
            _profilePhotoUri.value = value
        }

    companion object {
        private const val PREFS_NAME = "cipherlink_secure_prefs"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_ACCENT_COLOR = "accent_color"
        private const val KEY_CHAT_BG = "chat_bg"
        private const val KEY_ANIMATIONS = "animations"
        private const val KEY_LAST_INTEGRITY = "last_integrity_check"
        private const val KEY_PHOTO_URI = "profile_photo_uri"

        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"

        const val CHAT_BG_DEFAULT = "default"
        const val CHAT_BG_OCEAN = "ocean"
        const val CHAT_BG_FOREST = "forest"
        const val CHAT_BG_SUNSET = "sunset"
        const val CHAT_BG_NIGHT = "night"
        const val CHAT_BG_MINIMAL = "minimal"

        val CHAT_BACKGROUNDS = listOf(
            CHAT_BG_DEFAULT to "Default",
            CHAT_BG_OCEAN to "Ocean",
            CHAT_BG_FOREST to "Forest",
            CHAT_BG_SUNSET to "Sunset",
            CHAT_BG_NIGHT to "Night",
            CHAT_BG_MINIMAL to "Minimal"
        )

        val ACCENT_COLORS = listOf(
            "teal" to "Teal",
            "blue" to "Blue",
            "violet" to "Violet",
            "green" to "Green",
            "amber" to "Amber",
            "rose" to "Rose"
        )
    }
}
