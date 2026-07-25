package com.chris.chipherlink.security

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

/**
 * Version validation and security audit for CipherLink.
 * Ensures app integrity and key protection.
 */
class VersionManager(private val context: Context) {

    data class AppVersion(
        val versionName: String,
        val versionCode: Long,
        val packageName: String,
        val signingCertificate: String?
    )

    data class SecurityAudit(
        val isDebugBuild: Boolean,
        val isOnPlayStore: Boolean,
        val isKeyStoreAvailable: Boolean,
        val isDeviceSecure: Boolean,
        val issues: List<String>
    )

    /**
     * Get current app version info.
     */
    fun getCurrentVersion(): AppVersion {
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, 0)
        }

        val signingCert = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signatures = packageInfo.signingInfo?.apkContentsSigners
            signatures?.firstOrNull()?.let { signer ->
                val digest = MessageDigest.getInstance("SHA-256")
                val cert = signer.toByteArray()
                digest.update(cert)
                digest.digest().joinToString(":") { "%02X".format(it) }
            }
        } else null

        return AppVersion(
            versionName = packageInfo.versionName ?: "unknown",
            versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            },
            packageName = packageInfo.packageName,
            signingCertificate = signingCert
        )
    }

    /**
     * Perform a security audit of the app environment.
     */
    fun performSecurityAudit(): SecurityAudit {
        val issues = mutableListOf<String>()

        // Check debug build
        val isDebug = isDebugBuild()
        if (isDebug) {
            issues.add("Running in debug mode")
        }

        // Check KeyStore
        val keyStoreAvailable = isKeyStoreAvailable()
        if (!keyStoreAvailable) {
            issues.add("Android KeyStore not available")
        }

        // Check device security
        val deviceSecure = isDeviceSecure()
        if (!deviceSecure) {
            issues.add("Device has no screen lock")
        }

        // Check if running on emulator (basic check)
        if (isEmulator()) {
            issues.add("Running on emulator")
        }

        return SecurityAudit(
            isDebugBuild = isDebug,
            isOnPlayStore = false, // Local app
            isKeyStoreAvailable = keyStoreAvailable,
            isDeviceSecure = deviceSecure,
            issues = issues
        )
    }

    private fun isDebugBuild(): Boolean {
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun isKeyStoreAvailable(): Boolean {
        return try {
            val ks = java.security.KeyStore.getInstance("AndroidKeyStore")
            ks.load(null)
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun isDeviceSecure(): Boolean {
        val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
        return keyguardManager.isDeviceSecure
    }

    private fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }
}
