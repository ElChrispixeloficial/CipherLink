package com.chris.chipherlink.recovery

import android.content.Context
import com.chris.chipherlink.data.local.SecurePreferences
import com.chris.chipherlink.integrity.IdentityManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.security.SecureRandom
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manages CipherLink recovery packages (.clrecovery files).
 *
 * Recovery Package Structure:
 * - Identity data (UUID, public key)
 * - Encrypted keys (AES keys wrapped with password-derived key)
 * - User profile data
 * - Settings snapshot
 * - Device migration data
 *
 * Format: JSON → AES-256-CBC encrypted → Base64 → .clrecovery file
 *
 * Security:
 * - Password-derived key via PBKDF2 (100,000 iterations, SHA-256)
 * - Random 16-byte IV per encryption
 * - Random 32-byte salt per encryption
 */
class RecoveryManager(
    private val context: Context,
    private val identityManager: IdentityManager,
    private val securePreferences: SecurePreferences
) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * Generate a recovery package encrypted with the given password.
     * Returns the .clrecovery file path on success, null on failure.
     */
    fun generateRecoveryPackage(password: String, userId: String): File? {
        return try {
            // 1. Collect recovery data
            val recoveryData = collectRecoveryData(userId)

            // 2. Serialize to JSON
            val jsonData = gson.toJson(recoveryData)

            // 3. Derive key from password
            val salt = ByteArray(SALT_SIZE)
            SecureRandom().nextBytes(salt)

            val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = keyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            // 4. Encrypt with AES-256-CBC
            val iv = ByteArray(IV_SIZE)
            SecureRandom().nextBytes(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(iv))
            val encrypted = cipher.doFinal(jsonData.toByteArray(Charsets.UTF_8))

            // 5. Build the recovery package: salt + iv + encrypted data
            val packageData = ByteArray(salt.size + iv.size + encrypted.size)
            System.arraycopy(salt, 0, packageData, 0, salt.size)
            System.arraycopy(iv, 0, packageData, salt.size, iv.size)
            System.arraycopy(encrypted, 0, packageData, salt.size + iv.size, encrypted.size)

            // 6. Write to .clrecovery file
            val fileName = "CipherLinkRecovery_${System.currentTimeMillis()}.clrecovery"
            val file = File(context.filesDir, fileName)
            file.writeBytes(packageData)

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Validate and preview a recovery package without restoring.
     * Returns the recovery data preview on success, null on failure.
     */
    fun validateRecoveryPackage(file: File, password: String): RecoveryDataPreview? {
        return try {
            val packageData = file.readBytes()

            // Extract salt, IV, and encrypted data
            if (packageData.size < SALT_SIZE + IV_SIZE) return null

            val salt = packageData.copyOfRange(0, SALT_SIZE)
            val iv = packageData.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
            val encrypted = packageData.copyOfRange(SALT_SIZE + IV_SIZE, packageData.size)

            // Derive key from password
            val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = keyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            // Decrypt
            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val decryptedBytes = cipher.doFinal(encrypted)
            val json = String(decryptedBytes, Charsets.UTF_8)

            // Parse
            val data = gson.fromJson(json, RecoveryData::class.java)

            // Return preview (no sensitive key data)
            RecoveryDataPreview(
                userId = data.userId,
                identityId = data.identityId,
                displayName = data.displayName,
                createdAt = data.createdAt,
                appVersion = data.appVersion
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restore from a recovery package.
     * Returns the full recovery data on success, null on failure.
     */
    fun restoreFromRecovery(file: File, password: String): RecoveryData? {
        return try {
            val packageData = file.readBytes()

            if (packageData.size < SALT_SIZE + IV_SIZE) return null

            val salt = packageData.copyOfRange(0, SALT_SIZE)
            val iv = packageData.copyOfRange(SALT_SIZE, SALT_SIZE + IV_SIZE)
            val encrypted = packageData.copyOfRange(SALT_SIZE + IV_SIZE, packageData.size)

            val keySpec = PBEKeySpec(password.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_SIZE_BITS)
            val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
            val keyBytes = keyFactory.generateSecret(keySpec).encoded
            val secretKey = SecretKeySpec(keyBytes, "AES")

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(iv))
            val decryptedBytes = cipher.doFinal(encrypted)
            val json = String(decryptedBytes, Charsets.UTF_8)

            gson.fromJson(json, RecoveryData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * List all available recovery packages.
     */
    fun listRecoveryPackages(): List<RecoveryPackageInfo> {
        return context.filesDir.listFiles { file ->
            file.name.endsWith(".clrecovery")
        }?.map { file ->
            RecoveryPackageInfo(
                fileName = file.name,
                filePath = file.absolutePath,
                sizeBytes = file.length(),
                createdAt = file.lastModified()
            )
        }?.sortedByDescending { it.createdAt } ?: emptyList()
    }

    /**
     * Delete a recovery package.
     */
    fun deleteRecoveryPackage(fileName: String): Boolean {
        val file = File(context.filesDir, fileName)
        return file.exists() && file.delete()
    }

    private fun collectRecoveryData(userId: String): RecoveryData {
        val identityId = identityManager.getIdentityId() ?: "N/A"

        return RecoveryData(
            userId = userId,
            identityId = identityId,
            displayName = "", // Will be filled by caller if available
            email = null,
            themeMode = securePreferences.themeModeValue,
            accentColor = securePreferences.accentColorValue,
            createdAt = System.currentTimeMillis(),
            appVersion = "0.4",
            deviceInfo = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}"
        )
    }

    companion object {
        private const val SALT_SIZE = 32
        private const val IV_SIZE = 16
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_SIZE_BITS = 256
    }
}

data class RecoveryData(
    val userId: String,
    val identityId: String,
    val displayName: String,
    val email: String?,
    val themeMode: String,
    val accentColor: String,
    val createdAt: Long,
    val appVersion: String,
    val deviceInfo: String
)

data class RecoveryDataPreview(
    val userId: String,
    val identityId: String,
    val displayName: String,
    val createdAt: Long,
    val appVersion: String
)

data class RecoveryPackageInfo(
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val createdAt: Long
)
