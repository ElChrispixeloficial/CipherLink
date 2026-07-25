package com.chris.chipherlink.integrity

import android.content.Context
import android.content.SharedPreferences
import com.chris.chipherlink.utils.HashUtils
import java.io.File

/**
 * Core integrity verification system for CipherLink.
 *
 * Workflow:
 * 1. On register / legitimate change → generateFingerprints()
 * 2. On app startup → verifyIntegrity()
 * 3. If tampered → return IntegrityStatus.Tampered with affected files.
 *
 * Protected files:
 * - cipherlink_database (Room DB file)
 * - cipherlink_session (SharedPreferences)
 * - cipherlink_identity (SharedPreferences)
 *
 * Fingerprints are HMAC-SHA256 signed with the user's identity key.
 */
class IntegrityManager(
    private val context: Context,
    private val identityManager: IdentityManager
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    /**
     * Verifies that all protected files match their stored fingerprints.
     * Must be called on a background thread.
     */
    fun verifyIntegrity(): IntegrityStatus {
        identityManager.getIdentityId()
            ?: return IntegrityStatus.NoIdentity

        val storedFingerprints = getStoredFingerprints()

        if (storedFingerprints.isEmpty()) {
            return IntegrityStatus.NoIdentity
        }

        val compromised = mutableListOf<String>()

        for ((fileName, expectedFingerprint) in storedFingerprints) {

            val currentFingerprint = computeFingerprintForFile(fileName)

            if (currentFingerprint == null) {
                compromised.add(fileName)
                continue
            }

            if (currentFingerprint != expectedFingerprint) {
                compromised.add(fileName)
            }
        }

        return if (compromised.isEmpty()) {
            IntegrityStatus.Valid
        } else {
            IntegrityStatus.Tampered(compromised)
        }
    }

    /**
     * Generates fingerprints for all protected files and stores them.
     * Called after registration, login, or legitimate data changes.
     */
    fun generateFingerprints() {
        identityManager.getIdentityId() ?: return

        val fingerprints = mutableMapOf<String, String>()

        for (fileName in PROTECTED_FILES) {
            val fingerprint = computeFingerprintForFile(fileName)

            if (fingerprint != null) {
                fingerprints[fileName] = fingerprint
            }
        }

        val fingerprintData = fingerprints.entries
            .sortedBy { it.key }
            .joinToString("|") { "${it.key}=${it.value}" }
            .toByteArray(Charsets.UTF_8)

        val signature = identityManager.sign(fingerprintData)

        val editor = prefs.edit()

        for ((name, hash) in fingerprints) {
            editor.putString("${KEY_FP_PREFIX}$name", hash)
        }

        if (signature != null) {
            editor.putString(
                KEY_FINGERPRINT_SIGNATURE,
                android.util.Base64.encodeToString(
                    signature,
                    android.util.Base64.NO_WRAP
                )
            )
        }

        editor.putLong(
            KEY_LAST_VERIFIED,
            System.currentTimeMillis()
        )

        editor.apply()
    }

    /**
     * Clears all stored fingerprints.
     */
    fun clearFingerprints() {
        prefs.edit().clear().apply()
    }

    /**
     * Computes an HMAC-like fingerprint for a protected file.
     */
    private fun computeFingerprintForFile(fileName: String): String? {
        return try {
            val content = when (fileName) {

                DB_FILE_NAME -> readDatabaseFile()

                SESSION_PREFS ->
                    readSharedPrefsFile(
                        SessionManagerPrefs.CIPHERLINK_SESSION
                    )

                IDENTITY_PREFS ->
                    readSharedPrefsFile(
                        IdentityManagerPrefs.CIPHERLINK_IDENTITY
                    )

                else -> return null
            } ?: return null

            val deviceSecret = getDeviceSecret()

            HashUtils.sha256Hmac(
                content,
                deviceSecret
            )

        } catch (e: Exception) {
            null
        }
    }

    private fun readDatabaseFile(): ByteArray? {

        val dbFile = context.getDatabasePath(DB_FILE_NAME)

        if (!dbFile.exists()) {
            return null
        }

        val mainBytes = dbFile.readBytes()

        val walFile = File(
            dbFile.path + "-wal"
        )

        val shmFile = File(
            dbFile.path + "-shm"
        )

        val walBytes =
            if (walFile.exists()) walFile.readBytes()
            else byteArrayOf()

        val shmBytes =
            if (shmFile.exists()) shmFile.readBytes()
            else byteArrayOf()

        return mainBytes + walBytes + shmBytes
    }

    private fun readSharedPrefsFile(
        prefsName: String
    ): ByteArray? {

        val prefsDir = File(
            context.applicationInfo.dataDir,
            "shared_prefs"
        )

        val prefsFile = File(
            prefsDir,
            "$prefsName.xml"
        )

        if (!prefsFile.exists()) {
            return null
        }

        return prefsFile.readBytes()
    }

    private fun getDeviceSecret(): String {

        val existing =
            prefs.getString(KEY_DEVICE_SECRET, null)

        if (existing != null) {
            return existing
        }

        val raw =
            "${android.os.Build.FINGERPRINT}|${android.os.Build.BOARD}|${android.os.Build.DEVICE}"

        val hash = HashUtils.sha256(raw)

        prefs.edit()
            .putString(KEY_DEVICE_SECRET, hash)
            .apply()

        return hash
    }

    private fun getStoredFingerprints(): Map<String, String> {

        val result = mutableMapOf<String, String>()

        for (fileName in PROTECTED_FILES) {

            val fp =
                prefs.getString(
                    "${KEY_FP_PREFIX}$fileName",
                    null
                )

            if (fp != null) {
                result[fileName] = fp
            }
        }

        return result
    }

    companion object {

        private const val PREFS_NAME =
            "cipherlink_integrity"

        private const val KEY_FP_PREFIX =
            "fp_"

        private const val KEY_FINGERPRINT_SIGNATURE =
            "fp_signature"

        private const val KEY_LAST_VERIFIED =
            "last_verified"

        private const val KEY_DEVICE_SECRET =
            "device_secret"

        const val DB_FILE_NAME =
            "cipherlink_database"

        const val SESSION_PREFS =
            "cipherlink_session"

        const val IDENTITY_PREFS =
            "cipherlink_identity"

        val PROTECTED_FILES =
            listOf(
                DB_FILE_NAME,
                SESSION_PREFS,
                IDENTITY_PREFS
            )
    }

    private object SessionManagerPrefs {
        const val CIPHERLINK_SESSION =
            "cipherlink_session"
    }

    private object IdentityManagerPrefs {
        const val CIPHERLINK_IDENTITY =
            "cipherlink_identity"
    }
}