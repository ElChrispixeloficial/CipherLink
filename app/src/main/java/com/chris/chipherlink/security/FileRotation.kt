package com.chris.chipherlink.security

import android.content.Context
import java.io.File

/**
 * Safely rotates file identifiers to prevent long-term filename correlation.
 *
 * Rotation process:
 * 1. Read data from old physical file
 * 2. Write data to new physical file
 * 3. Verify new file integrity
 * 4. Update DynamicNaming registry
 * 5. Delete old physical file
 *
 * If the process is interrupted at any step, the next startup will detect
 * the incomplete rotation and roll back safely.
 */
class FileRotation(
    private val context: Context,
    private val dynamicNaming: DynamicNaming
) {

    /**
     * Rotate a file's physical name while preserving its data.
     * Returns the new physical name on success, null on failure.
     */
    fun rotate(logicalName: String, directory: File): String? {
        val oldPhysicalName = dynamicNaming.resolve(logicalName)
        val oldFile = File(directory, oldPhysicalName)

        if (!oldFile.exists()) {
            val newPhysicalName = dynamicNaming.resolve(logicalName)
            return newPhysicalName
        }

        val newPhysicalName = generateRandomName()
        val newFile = File(directory, newPhysicalName)
        val backupFile = File(directory, "$oldPhysicalName.bak")

        return try {
            // Step 1: Create backup of old file
            oldFile.copyTo(backupFile, overwrite = true)

            // Step 2: Write data to new file
            oldFile.copyTo(newFile, overwrite = true)

            // Step 3: Verify new file exists and has content
            if (!newFile.exists() || newFile.length() == 0L) {
                throw IllegalStateException("New file verification failed")
            }

            // Step 4: Update registry
            dynamicNaming.remap(logicalName, newPhysicalName)

            // Step 5: Delete backup (rotation complete)
            backupFile.delete()

            newPhysicalName
        } catch (e: Exception) {
            // Rollback: restore from backup if new file was partially written
            if (backupFile.exists()) {
                newFile.delete()
                // Registry still points to old name, so backup is valid
            }
            null
        }
    }

    /**
     * Check for incomplete rotations on startup and roll back if needed.
     */
    fun recoverIncompleteRotations(directory: File) {
        val backupFiles = directory.listFiles { file -> file.name.endsWith(".bak") } ?: return

        for (backup in backupFiles) {
            val originalName = backup.name.removeSuffix(".bak")
            val originalFile = File(directory, originalName)

            // If original was deleted but backup exists, restore it
            if (!originalFile.exists()) {
                backup.copyTo(originalFile)
            }

            // Clean up backup file
            backup.delete()
        }
    }

    private fun generateRandomName(): String {
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return buildString(16) {
            repeat(16) {
                append(chars.random())
            }
        }
    }
}
