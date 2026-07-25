package com.chris.chipherlink.security

import android.content.Context
import com.chris.chipherlink.integrity.IdentityManager
import com.chris.chipherlink.integrity.IntegrityManager
import java.io.File

/**
 * Central vault abstraction for CipherLink.
 *
 * CipherLinkVault
 * ├── Identity/     (IdentityManager — RSA-2048 in KeyStore)
 * ├── User Keys/    (KeyManager — AES-256-GCM in KeyStore)
 * ├── Profile Data/ (SecureStorage — encrypted file I/O)
 * ├── Settings/     (SecurePreferences — EncryptedSharedPreferences)
 * ├── Chats/        (SQLCipher — encrypted Room database)
 * └── Recovery/     (RecoveryManager — encrypted .clrecovery files)
 *
 * The vault coordinates all security subsystems and provides
 * a unified interface for initialization and health checks.
 */
class VaultManager(
    private val context: Context,
    private val identityManager: IdentityManager,
    private val integrityManager: IntegrityManager,
    private val keyManager: KeyManager,
    private val secureStorage: SecureStorage
) {

    val dynamicNaming = DynamicNaming(context)
    val fileRotation = FileRotation(context, dynamicNaming)

    private var _isUnlocked = false
    val isUnlocked: Boolean get() = _isUnlocked

    /**
     * Initialize the vault on first launch or after reinstall.
     * Creates identity, generates initial fingerprints, sets up dynamic naming.
     */
    fun initializeVault(): VaultHealth {
        // 1. Ensure identity exists
        if (!identityManager.hasIdentity()) {
            identityManager.generateIdentity()
        }

        // 2. Ensure encryption keys exist
        ensureKeyExists(KeyManager.DB_ENCRYPTION_KEY)
        ensureKeyExists(KeyManager.BACKUP_ENCRYPTION_KEY)
        ensureKeyExists(KeyManager.PROFILE_ENCRYPTION_KEY)

        // 3. Register logical file names if not already registered
        registerLogicalNames()

        // 4. Recover any incomplete file rotations
        fileRotation.recoverIncompleteRotations(context.filesDir)

        // 5. Generate integrity fingerprints
        integrityManager.generateFingerprints()

        _isUnlocked = true

        return checkHealth()
    }

    /**
     * Verify vault integrity without modifying anything.
     */
    fun checkHealth(): VaultHealth {
        val hasIdentity = identityManager.hasIdentity()
        val hasDbKey = keyManager.keyExists(KeyManager.DB_ENCRYPTION_KEY)
        val hasBackupKey = keyManager.keyExists(KeyManager.BACKUP_ENCRYPTION_KEY)
        val hasProfileKey = keyManager.keyExists(KeyManager.PROFILE_ENCRYPTION_KEY)

        val allKeysPresent = hasIdentity && hasDbKey && hasBackupKey && hasProfileKey
        val mappingsCount = dynamicNaming.getAllMappings().size

        return VaultHealth(
            isHealthy = allKeysPresent,
            hasIdentity = hasIdentity,
            hasDbKey = hasDbKey,
            hasBackupKey = hasBackupKey,
            hasProfileKey = hasProfileKey,
            registeredFiles = mappingsCount,
            isUnlocked = _isUnlocked
        )
    }

    /**
     * Rotate file identifiers for privacy.
     */
    fun rotateFiles(): Boolean {
        return try {
            for (logicalName in LOGICAL_NAMES) {
                fileRotation.rotate(logicalName, context.filesDir)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lock the vault (clear runtime state, not persistent keys).
     */
    fun lock() {
        _isUnlocked = false
    }

    /**
     * Unlock the vault (restore runtime state from KeyStore).
     */
    fun unlock() {
        _isUnlocked = identityManager.hasIdentity()
    }

    private fun ensureKeyExists(alias: String) {
        if (!keyManager.keyExists(alias)) {
            keyManager.getOrCreateKey(alias)
        }
    }

    private fun registerLogicalNames() {
        for (logicalName in LOGICAL_NAMES) {
            if (!dynamicNaming.hasMapping(logicalName)) {
                dynamicNaming.resolve(logicalName)
            }
        }
    }

    data class VaultHealth(
        val isHealthy: Boolean,
        val hasIdentity: Boolean,
        val hasDbKey: Boolean,
        val hasBackupKey: Boolean,
        val hasProfileKey: Boolean,
        val registeredFiles: Int,
        val isUnlocked: Boolean
    )

    companion object {
        val LOGICAL_NAMES = listOf(
            "identity_data",
            "user_keys",
            "profile_store",
            "settings_store",
            "recovery_data"
        )
    }
}
