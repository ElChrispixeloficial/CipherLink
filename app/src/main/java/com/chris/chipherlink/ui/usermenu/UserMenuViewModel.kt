package com.chris.chipherlink.ui.usermenu

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.backup.BackupInfo
import com.chris.chipherlink.backup.BackupManager
import com.chris.chipherlink.data.local.SecurePreferences
import com.chris.chipherlink.integrity.IntegrityStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UserMenuUiState(
    val username: String = "",
    val identityId: String = "",
    val themeMode: String = SecurePreferences.THEME_SYSTEM,
    val accentColor: String = "teal",
    val chatBackground: String = SecurePreferences.CHAT_BG_DEFAULT,
    val animationsEnabled: Boolean = true,
    val integrityStatus: IntegrityStatus? = null,
    val lastIntegrityCheck: Long = 0L,
    val backups: List<BackupInfo> = emptyList(),
    val backupSizeBytes: Long = 0L,
    val isCheckingIntegrity: Boolean = false,
    val isCreatingBackup: Boolean = false
)

class UserMenuViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val securePrefs = SecurePreferences(application)
    private val backupManager = BackupManager(application)

    private val userId: String = app.authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(UserMenuUiState())
    val uiState: StateFlow<UserMenuUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        viewModelScope.launch {
            val currentUser = app.database.userDao().getById(userId)
            _uiState.value = UserMenuUiState(
                username = app.sessionManager.getCurrentUsername() ?: "",
                identityId = currentUser?.cipherLinkId ?: "N/A",
                themeMode = securePrefs.themeModeValue,
                accentColor = securePrefs.accentColorValue,
                chatBackground = securePrefs.chatBackgroundValue,
                animationsEnabled = securePrefs.animationsEnabledValue,
                lastIntegrityCheck = securePrefs.lastIntegrityCheckValue,
                backups = backupManager.listBackups(),
                backupSizeBytes = backupManager.getBackupSize()
            )
        }
    }

    fun setThemeMode(mode: String) {
        securePrefs.themeModeValue = mode
        _uiState.value = _uiState.value.copy(themeMode = mode)
    }

    fun setAccentColor(color: String) {
        securePrefs.accentColorValue = color
        _uiState.value = _uiState.value.copy(accentColor = color)
    }

    fun setChatBackground(background: String) {
        securePrefs.chatBackgroundValue = background
        _uiState.value = _uiState.value.copy(chatBackground = background)
    }

    fun toggleAnimations() {
        val newValue = !_uiState.value.animationsEnabled
        securePrefs.animationsEnabledValue = newValue
        _uiState.value = _uiState.value.copy(animationsEnabled = newValue)
    }

    fun checkIntegrity() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingIntegrity = true)
            val status = withContext(Dispatchers.IO) {
                app.integrityManager.verifyIntegrity()
            }
            securePrefs.lastIntegrityCheckValue = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                integrityStatus = status,
                lastIntegrityCheck = System.currentTimeMillis(),
                isCheckingIntegrity = false
            )
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCreatingBackup = true)
            withContext(Dispatchers.IO) {
                backupManager.createBackup()
            }
            _uiState.value = _uiState.value.copy(
                backups = backupManager.listBackups(),
                backupSizeBytes = backupManager.getBackupSize(),
                isCreatingBackup = false
            )
        }
    }

    fun deleteBackup(fileName: String) {
        backupManager.deleteBackup(fileName)
        _uiState.value = _uiState.value.copy(
            backups = backupManager.listBackups(),
            backupSizeBytes = backupManager.getBackupSize()
        )
    }

    fun formatTimeSince(timestamp: Long): String {
        if (timestamp == 0L) return "Never"
        val diff = System.currentTimeMillis() - timestamp
        val minutes = diff / 60_000
        val hours = diff / 3_600_000
        val days = diff / 86_400_000
        return when {
            minutes < 1 -> "Just now"
            minutes < 60 -> "${minutes}m ago"
            hours < 24 -> "${hours}h ago"
            else -> "${days}d ago"
        }
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
        }
    }
}
