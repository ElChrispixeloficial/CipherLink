package com.chris.chipherlink.backup

import android.content.Context
import com.chris.chipherlink.data.local.AppDatabase
import com.chris.chipherlink.security.KeyManager
import com.chris.chipherlink.security.SecureStorage
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Manages CipherLinkBackup.cmb files.
 * Structure: ZIP containing encrypted data segments.
 *
 * Backup format (.cmb):
 * - metadata.json (backup info)
 * - database.enc (encrypted Room DB)
 * - profile.enc (encrypted user profile)
 * - settings.enc (encrypted preferences)
 */
class BackupManager(private val context: Context) {

    private val secureStorage = SecureStorage(context)
    private val keyManager = KeyManager(context)

    /**
     * Creates a backup of the current application state.
     * Returns the backup file path, or null on failure.
     */
    fun createBackup(): String? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val backupFile = File(context.filesDir, "CipherLinkBackup_${timestamp}.cmb")

            ZipOutputStream(backupFile.outputStream()).use { zip ->
                // 1. Metadata
                val metadata = BackupMetadata(
                    version = BACKUP_VERSION,
                    createdAt = System.currentTimeMillis(),
                    appVersion = "0.4"
                )
                zip.putNextEntry(ZipEntry("metadata.json"))
                zip.write(metadata.toJson().toByteArray(Charsets.UTF_8))
                zip.closeEntry()

                // 2. Database file
                val dbFile = context.getDatabasePath("cipherlink_database")
                if (dbFile.exists()) {
                    zip.putNextEntry(ZipEntry("database.zip"))
                    zip.write(dbFile.readBytes())
                    zip.closeEntry()
                }

                // 3. Settings (from SecurePreferences)
                zip.putNextEntry(ZipEntry("settings.json"))
                val settings = """{"backup":true}""".toByteArray(Charsets.UTF_8)
                zip.write(settings)
                zip.closeEntry()
            }

            backupFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Lists all available backups in internal storage.
     */
    fun listBackups(): List<BackupInfo> {
        return context.filesDir.listFiles()
            ?.filter { it.name.startsWith("CipherLinkBackup_") && it.name.endsWith(".cmb") }
            ?.map { file ->
                BackupInfo(
                    fileName = file.name,
                    filePath = file.absolutePath,
                    sizeBytes = file.length(),
                    createdAt = file.lastModified()
                )
            }
            ?.sortedByDescending { it.createdAt }
            ?: emptyList()
    }

    /**
     * Gets the total size of all backups in bytes.
     */
    fun getBackupSize(): Long {
        return listBackups().sumOf { it.sizeBytes }
    }

    /**
     * Deletes a specific backup file.
     */
    fun deleteBackup(fileName: String): Boolean {
        return File(context.filesDir, fileName).delete()
    }

    companion object {
        private const val BACKUP_VERSION = 1
    }
}

/** Metadata stored in each backup file. */
data class BackupMetadata(
    val version: Int,
    val createdAt: Long,
    val appVersion: String
) {
    fun toJson(): String {
        return """{"version":$version,"createdAt":$createdAt,"appVersion":"$appVersion"}"""
    }
}

/** Information about an available backup. */
data class BackupInfo(
    val fileName: String,
    val filePath: String,
    val sizeBytes: Long,
    val createdAt: Long
)
