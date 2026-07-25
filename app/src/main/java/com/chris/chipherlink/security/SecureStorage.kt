package com.chris.chipherlink.security

import android.content.Context
import java.io.File

/**
 * Secure file I/O operations for CipherLink.
 * Provides encrypted read/write for sensitive files.
 */
class SecureStorage(private val context: Context) {

    private val keyManager = KeyManager(context)

    /**
     * Writes data to a file with AES-256-GCM encryption.
     */
    fun writeEncrypted(fileName: String, data: String, keyAlias: String) {
        val file = File(context.filesDir, fileName)
        val (encrypted, iv) = keyManager.encrypt(data.toByteArray(Charsets.UTF_8), keyAlias)
        file.writeText("$iv|$encrypted")
    }

    /**
     * Reads encrypted data from a file.
     * Returns null if file doesn't exist or decryption fails.
     */
    fun readEncrypted(fileName: String, keyAlias: String): String? {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return null
        return try {
            val content = file.readText()
            val parts = content.split("|", limit = 2)
            if (parts.size != 2) return null
            val iv = parts[0]
            val encrypted = parts[1]
            val decrypted = keyManager.decrypt(encrypted, iv, keyAlias)
            String(decrypted, Charsets.UTF_8)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Checks if a file exists in internal storage.
     */
    fun fileExists(fileName: String): Boolean {
        return File(context.filesDir, fileName).exists()
    }

    /**
     * Deletes a file from internal storage.
     */
    fun deleteFile(fileName: String): Boolean {
        return File(context.filesDir, fileName).delete()
    }

    /**
     * Returns the internal storage path for a file.
     */
    fun getFilePath(fileName: String): String {
        return File(context.filesDir, fileName).absolutePath
    }

    /**
     * Copies a file from a source URI to internal storage.
     * Returns the new file name, or null on failure.
     */
    fun copyToInternal(sourceUri: android.net.Uri, destFileName: String): String? {
        return try {
            val destFile = File(context.filesDir, destFileName)
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFileName
        } catch (e: Exception) {
            null
        }
    }
}
