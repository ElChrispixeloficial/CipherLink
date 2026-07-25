package com.chris.chipherlink.utils

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Cryptographic hash utilities used by the integrity system.
 * All methods use standard, well-tested algorithms — no custom crypto.
 */
object HashUtils {

    private const val HMAC_ALGORITHM = "HmacSHA256"
    private const val SHA256_ALGORITHM = "SHA-256"

    /**
     * Computes SHA-256 hash of the input string.
     */
    fun sha256(input: String): String {
        val digest = java.security.MessageDigest.getInstance(SHA256_ALGORITHM)
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes HMAC-SHA256 of [data] using [key].
     * Returns hex-encoded result.
     */
    fun sha256Hmac(data: ByteArray, key: String): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), HMAC_ALGORITHM)
        mac.init(secretKey)
        val result = mac.doFinal(data)
        return result.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes SHA-256 hash of raw bytes.
     */
    fun sha256Bytes(data: ByteArray): String {
        val digest = java.security.MessageDigest.getInstance(SHA256_ALGORITHM)
        val hash = digest.digest(data)
        return hash.joinToString("") { "%02x".format(it) }
    }
}
