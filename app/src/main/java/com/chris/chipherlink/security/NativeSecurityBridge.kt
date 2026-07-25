package com.chris.chipherlink.security

import android.util.Base64
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Security bridge with JNI native (C++) acceleration and Kotlin fallback.
 *
 * Security Note:
 * - No private keys stored in native code
 * - All keys remain in Android Keystore
 * - Native layer complements, does not replace, Kotlin crypto APIs
 */
class NativeSecurityBridge private constructor() {

    companion object {
        private const val HMAC_ALGORITHM = "HmacSHA256"
        private var nativeLoaded = false

        init {
            try {
                System.loadLibrary("ciphercore")
                nativeLoaded = true
            } catch (_: UnsatisfiedLinkError) {
                nativeLoaded = false
            } catch (_: Exception) {
                nativeLoaded = false
            }
        }

        @Volatile
        private var instance: NativeSecurityBridge? = null

        fun getInstance(): NativeSecurityBridge {
            return instance ?: synchronized(this) {
                instance ?: NativeSecurityBridge().also { instance = it }
            }
        }
    }

    fun isNativeAvailable(): Boolean = nativeLoaded

    fun getEngineType(): String = if (nativeLoaded) "JNI-C++" else "Kotlin-Fallback"

    fun getNativeVersion(): String = if (nativeLoaded) nativeGetVersion() else "kotlin-fallback-1.0"

    // ── SHA-256 ───────────────────────────────────────────────

    fun calculateSecureHash(data: ByteArray): String {
        return if (nativeLoaded) {
            try {
                nativeCalculateSecureHash(data)
            } catch (_: Exception) {
                kotlinSha256(data)
            }
        } else {
            kotlinSha256(data)
        }
    }

    private fun kotlinSha256(data: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(data)
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    // ── HMAC-SHA256 ───────────────────────────────────────────

    fun calculateHmac(data: ByteArray, key: ByteArray): String {
        return if (nativeLoaded) {
            try {
                nativeCalculateHmac(data, key)
            } catch (_: Exception) {
                kotlinHmac(data, key)
            }
        } else {
            kotlinHmac(data, key)
        }
    }

    private fun kotlinHmac(data: ByteArray, key: ByteArray): String {
        val mac = Mac.getInstance(HMAC_ALGORITHM)
        mac.init(SecretKeySpec(key, HMAC_ALGORITHM))
        val result = mac.doFinal(data)
        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    // ── Constant-time compare ──────────────────────────────────

    fun constantTimeCompare(a: ByteArray, b: ByteArray): Boolean {
        return if (nativeLoaded) {
            try {
                nativeConstantTimeCompare(a, b)
            } catch (_: Exception) {
                kotlinConstantTimeCompare(a, b)
            }
        } else {
            kotlinConstantTimeCompare(a, b)
        }
    }

    private fun kotlinConstantTimeCompare(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }

    // ── Integrity verification ─────────────────────────────────

    fun verifyDataIntegrity(data: ByteArray, expectedHash: String): Boolean {
        val actualHash = calculateSecureHash(data)
        return constantTimeCompare(
            Base64.decode(actualHash, Base64.NO_WRAP),
            Base64.decode(expectedHash, Base64.NO_WRAP)
        )
    }

    // ── Salted hash helpers ────────────────────────────────────

    fun calculateSecureHashWithSalt(data: ByteArray): Pair<String, String> {
        val salt = ByteArray(32)
        SecureRandom().nextBytes(salt)

        val dataWithSalt = data + salt
        val hash = calculateSecureHash(dataWithSalt)
        val saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)

        return Pair(hash, saltBase64)
    }

    fun verifySaltedHash(data: ByteArray, hash: String, saltBase64: String): Boolean {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val dataWithSalt = data + salt
        val expectedHash = calculateSecureHash(dataWithSalt)
        return constantTimeCompare(
            Base64.decode(expectedHash, Base64.NO_WRAP),
            Base64.decode(hash, Base64.NO_WRAP)
        )
    }

    // ── Health check ───────────────────────────────────────────

    fun healthCheck(): Int {
        return if (nativeLoaded) {
            try {
                nativeHealthCheck()
            } catch (_: Exception) {
                kotlinHealthCheck()
            }
        } else {
            kotlinHealthCheck()
        }
    }

    private fun kotlinHealthCheck(): Int {
        return try {
            val testData = "CipherLink Health Check".toByteArray()
            val hash = calculateSecureHash(testData)
            if (hash.isEmpty()) return 1

            val hmac = calculateHmac(testData, "test_key".toByteArray())
            if (hmac.isEmpty()) return 2

            if (!constantTimeCompare(testData, testData)) return 3

            0
        } catch (_: Exception) {
            4
        }
    }

    // ── JNI externals ──────────────────────────────────────────

    private external fun nativeCalculateSecureHash(data: ByteArray): String
    private external fun nativeCalculateHmac(data: ByteArray, key: ByteArray): String
    private external fun nativeConstantTimeCompare(a: ByteArray, b: ByteArray): Boolean
    private external fun nativeHealthCheck(): Int
    private external fun nativeGetVersion(): String
}
