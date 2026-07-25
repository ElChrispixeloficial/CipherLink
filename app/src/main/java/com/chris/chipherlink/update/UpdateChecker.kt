package com.chris.chipherlink.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Checks for app updates from GitHub Releases.
 * Repository: ElChrispixeloficial/CipherLink
 */
class UpdateChecker(private val context: Context) {

    data class GitHubRelease(
        @SerializedName("tag_name") val tagName: String,
        @SerializedName("name") val name: String,
        @SerializedName("body") val body: String,
        @SerializedName("published_at") val publishedAt: String,
        @SerializedName("assets") val assets: List<ReleaseAsset>
    )

    data class ReleaseAsset(
        @SerializedName("name") val name: String,
        @SerializedName("browser_download_url") val downloadUrl: String,
        @SerializedName("size") val size: Long
    )

    data class UpdateInfo(
        val available: Boolean,
        val currentVersion: String,
        val latestVersion: String,
        val releaseName: String,
        val releaseNotes: String,
        val downloadUrl: String?,
        val publishedAt: String
    )

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/ElChrispixeloficial/CipherLink/releases/latest"
        private const val PREFS_NAME = "cipherlink_updates"
        private const val KEY_LAST_CHECK = "last_check_time"
        private const val KEY_SKIPPED_VERSION = "skipped_version"
        private const val CHECK_INTERVAL_MS = 6 * 60 * 60 * 1000L // 6 hours
    }

    /**
     * Check for updates from GitHub Releases.
     * @return UpdateInfo with availability and details
     */
    suspend fun checkForUpdates(currentVersion: String): UpdateInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(GITHUB_API_URL)
                    .header("Accept", "application/vnd.github.v3+json")
                    .header("User-Agent", "CipherLink/$currentVersion")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) return@withContext null

                val body = response.body?.string() ?: return@withContext null
                val release = gson.fromJson(body, GitHubRelease::class.java)

                val latestVersion = release.tagName.removePrefix("v")
                val downloadUrl = release.assets
                    .firstOrNull { it.name.endsWith(".apk") }
                    ?.downloadUrl

                val available = isNewerVersion(currentVersion, latestVersion)
                val skipped = getSkippedVersion()

                UpdateInfo(
                    available = available && latestVersion != skipped,
                    currentVersion = currentVersion,
                    latestVersion = latestVersion,
                    releaseName = release.name,
                    releaseNotes = release.body,
                    downloadUrl = downloadUrl,
                    publishedAt = release.publishedAt
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Compare version strings (semver-like: major.minor.patch).
     */
    private fun isNewerVersion(current: String, latest: String): Boolean {
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val latestParts = latest.split(".").mapNotNull { it.toIntOrNull() }

        for (i in 0 until maxOf(currentParts.size, latestParts.size)) {
            val c = currentParts.getOrElse(i) { 0 }
            val l = latestParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }

    /**
     * Open download URL in browser.
     */
    fun openDownloadPage(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Open GitHub releases page.
     */
    fun openReleasesPage() {
        val url = "https://github.com/ElChrispixeloficial/CipherLink/releases"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    /**
     * Skip a specific version (don't remind about it).
     */
    fun skipVersion(version: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_SKIPPED_VERSION, version)
            .apply()
    }

    private fun getSkippedVersion(): String? {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_SKIPPED_VERSION, null)
    }

    /**
     * Check if enough time has passed since last check.
     */
    fun shouldCheck(): Boolean {
        val lastCheck = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_CHECK, 0)
        return System.currentTimeMillis() - lastCheck > CHECK_INTERVAL_MS
    }

    fun recordCheckTime() {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putLong(KEY_LAST_CHECK, System.currentTimeMillis())
            .apply()
    }
}
