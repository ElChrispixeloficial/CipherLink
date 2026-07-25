package com.chris.chipherlink.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Manages AES-256-GCM encryption keys via Android Keystore.
 * Used for encrypting sensitive local data.
 */
class KeyManager(context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    /**
     * Generates or retrieves an AES-256 key for the given alias.
     */
    fun getOrCreateKey(alias: String): SecretKey {
        keyStore.getEntry(alias, null)?.let { entry ->
            return (entry as KeyStore.SecretKeyEntry).secretKey
        }

        val spec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()

        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        kg.init(spec)
        return kg.generateKey()
    }

    /**
     * Encrypts data using AES-256-GCM.
     * Returns a pair of (ciphertext, IV) both as Base64 strings.
     */
    fun encrypt(data: ByteArray, alias: String): Pair<String, String> {
        val key = getOrCreateKey(alias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(data)

        return Pair(
            android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP),
            android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
        )
    }

    /**
     * Decrypts data using AES-256-GCM.
     */
    fun decrypt(encryptedBase64: String, ivBase64: String, alias: String): ByteArray {
        val key = getOrCreateKey(alias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = android.util.Base64.decode(ivBase64, android.util.Base64.NO_WRAP)
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)
        val encrypted = android.util.Base64.decode(encryptedBase64, android.util.Base64.NO_WRAP)
        return cipher.doFinal(encrypted)
    }

    /**
     * Checks if a key with the given alias exists in KeyStore.
     */
    fun keyExists(alias: String): Boolean {
        return keyStore.containsAlias(alias)
    }

    /**
     * Deletes a key from KeyStore.
     */
    fun deleteKey(alias: String) {
        keyStore.deleteEntry(alias)
    }

    companion object {
        const val DB_ENCRYPTION_KEY = "cipherlink_db_key"
        const val BACKUP_ENCRYPTION_KEY = "cipherlink_backup_key"
        const val PROFILE_ENCRYPTION_KEY = "cipherlink_profile_key"
    }
}
