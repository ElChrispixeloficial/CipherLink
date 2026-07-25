package com.chris.chipherlink.integrity

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.util.UUID

/**
 * Generates and manages the user's cryptographic identity.
 *
 * The identity consists of:
 * - A unique identity ID (UUID).
 * - An RSA key pair stored in Android KeyStore (private key never leaves hardware).
 *
 * The private key signs integrity fingerprints. The public key verifies them.
 */
class IdentityManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME, Context.MODE_PRIVATE
    )

    fun hasIdentity(): Boolean {
        return prefs.getString(KEY_IDENTITY_ID, null) != null
    }

    fun getIdentityId(): String? {
        return prefs.getString(KEY_IDENTITY_ID, null)
    }

    /**
     * Generates a new identity: UUID + RSA key pair in Android KeyStore.
     * Returns the identity ID.
     */
    fun generateIdentity(): String {
        val identityId = UUID.randomUUID().toString()

        val spec = KeyGenParameterSpec.Builder(
            identityId,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
            .setKeySize(2048)
            .build()

        val kpg = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_RSA, "AndroidKeyStore"
        )
        kpg.initialize(spec)
        kpg.generateKeyPair()

        prefs.edit()
            .putString(KEY_IDENTITY_ID, identityId)
            .putLong(KEY_CREATED_AT, System.currentTimeMillis())
            .apply()

        return identityId
    }

    /**
     * Signs data with the identity's private key.
     * Returns the signature bytes, or null on failure.
     */
    fun sign(data: ByteArray): ByteArray? {
        val identityId = getIdentityId() ?: return null
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val entry = keyStore.getEntry(identityId, null) as? KeyStore.PrivateKeyEntry
                ?: return null
            val signature = Signature.getInstance("SHA256withRSA")
            signature.initSign(entry.privateKey)
            signature.update(data)
            signature.sign()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifies a signature against data using the identity's public key.
     */
    fun verify(data: ByteArray, signatureBytes: ByteArray): Boolean {
        val identityId = getIdentityId() ?: return false
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            val entry = keyStore.getEntry(identityId, null) as? KeyStore.PrivateKeyEntry
                ?: return false
            val sig = Signature.getInstance("SHA256withRSA")
            sig.initVerify(entry.certificate.publicKey)
            sig.update(data)
            sig.verify(signatureBytes)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Deletes the identity and all associated keys.
     */
    fun deleteIdentity() {
        val identityId = getIdentityId() ?: return
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            keyStore.deleteEntry(identityId)
        } catch (_: Exception) { }
        prefs.edit().clear().apply()
    }

    companion object {
        private const val PREFS_NAME = "cipherlink_identity"
        private const val KEY_IDENTITY_ID = "identity_id"
        private const val KEY_CREATED_AT = "created_at"
    }
}
