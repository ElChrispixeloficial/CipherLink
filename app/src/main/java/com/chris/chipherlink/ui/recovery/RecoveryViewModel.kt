package com.chris.chipherlink.ui.recovery

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.recovery.RecoveryManager
import com.chris.chipherlink.recovery.RecoveryPackageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class RecoveryUiState(
    val mode: RecoveryMode = RecoveryMode.NONE,
    val isProcessing: Boolean = false,
    val password: String = "",
    val confirmPassword: String = "",
    val generatedFile: File? = null,
    val generatedCode: String = "",
    val packages: List<RecoveryPackageInfo> = emptyList(),
    val previewData: com.chris.chipherlink.recovery.RecoveryDataPreview? = null,
    val restoreData: com.chris.chipherlink.recovery.RecoveryData? = null,
    val error: String? = null,
    val successMessage: String? = null
)

enum class RecoveryMode {
    NONE, GENERATE, VALIDATE, RESTORE
}

class RecoveryViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val recoveryManager = RecoveryManager(
        context = application,
        identityManager = app.identityManager,
        securePreferences = app.securePreferences
    )

    private val userId: String = app.authRepository.getCurrentUserId() ?: ""

    private val _uiState = MutableStateFlow(RecoveryUiState())
    val uiState: StateFlow<RecoveryUiState> = _uiState.asStateFlow()

    init {
        loadPackages()
    }

    private fun loadPackages() {
        _uiState.value = _uiState.value.copy(
            packages = recoveryManager.listRecoveryPackages()
        )
    }

    fun setMode(mode: RecoveryMode) {
        _uiState.value = _uiState.value.copy(
            mode = mode,
            error = null,
            successMessage = null,
            generatedFile = null,
            previewData = null,
            restoreData = null
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateConfirmPassword(password: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = password)
    }

    fun generateRecoveryPackage() {
        val state = _uiState.value
        if (state.password.length < 8) {
            _uiState.value = state.copy(error = "Password must be at least 8 characters")
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.value = state.copy(error = "Passwords do not match")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            val file = withContext(Dispatchers.IO) {
                recoveryManager.generateRecoveryPackage(state.password, userId)
            }
            if (file != null) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    generatedFile = file,
                    successMessage = "Recovery package created successfully"
                )
                loadPackages()
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Failed to create recovery package"
                )
            }
        }
    }

    fun validateRecoveryPackage(file: File) {
        val password = _uiState.value.password
        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter the recovery password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            val preview = withContext(Dispatchers.IO) {
                recoveryManager.validateRecoveryPackage(file, password)
            }
            if (preview != null) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    previewData = preview
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Invalid recovery package or wrong password"
                )
            }
        }
    }

    fun restoreFromRecovery(file: File) {
        val password = _uiState.value.password
        if (password.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Enter the recovery password")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, error = null)
            val data = withContext(Dispatchers.IO) {
                recoveryManager.restoreFromRecovery(file, password)
            }
            if (data != null) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    restoreData = data,
                    successMessage = "Recovery data decrypted successfully"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Failed to decrypt recovery package"
                )
            }
        }
    }

    fun deleteRecoveryPackage(fileName: String) {
        recoveryManager.deleteRecoveryPackage(fileName)
        loadPackages()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${"%.1f".format(bytes / (1024.0 * 1024))} MB"
        }
    }
}
