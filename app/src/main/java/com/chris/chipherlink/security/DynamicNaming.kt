package com.chris.chipherlink.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom

/**
 * Generates random filenames and maintains an encrypted registry mapping
 * logical names to physical names. Prevents filename-based data leakage.
 */
class DynamicNaming(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val registry: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        REGISTRY_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    /**
     * Get the physical filename for a logical name.
     * Generates a new random name if none exists.
     */
    fun resolve(logicalName: String): String {
        val existing = registry.getString(PREFIX_KEY + logicalName, null)
        if (existing != null) return existing

        val generated = generateRandomName()
        registry.edit().putString(PREFIX_KEY + logicalName, generated).apply()
        return generated
    }

    /**
     * Check if a logical name already has a physical mapping.
     */
    fun hasMapping(logicalName: String): Boolean {
        return registry.contains(PREFIX_KEY + logicalName)
    }

    /**
     * Update the physical name for a logical name (used during rotation).
     */
    fun remap(logicalName: String, newPhysicalName: String) {
        registry.edit().putString(PREFIX_KEY + logicalName, newPhysicalName).apply()
    }

    /**
     * Get all registered logical→physical mappings.
     */
    fun getAllMappings(): Map<String, String> {
        return registry.all.filterKeys { it.startsWith(PREFIX_KEY) }
            .mapKeys { it.key.removePrefix(PREFIX_KEY) }
            .mapValues { it.value as? String ?: "" }
    }

    /**
     * Remove a mapping from the registry.
     */
    fun remove(logicalName: String) {
        registry.edit().remove(PREFIX_KEY + logicalName).apply()
    }

    /**
     * Check if an orphan physical file exists (not in registry).
     */
    fun isOrphaned(physicalName: String): Boolean {
        return registry.all.values.none { it == physicalName }
    }

    private fun generateRandomName(): String {
        val chars = ALPHABET
        return buildString(NAME_LENGTH) {
            repeat(NAME_LENGTH) {
                append(chars[SecureRandom().nextInt(chars.length)])
            }
        }
    }

    companion object {
        private const val REGISTRY_NAME = "cipherlink_file_registry"
        private const val PREFIX_KEY = "mapping_"
        private const val NAME_LENGTH = 16
        private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
    }
}
