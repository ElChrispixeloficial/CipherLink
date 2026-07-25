package com.chris.chipherlink.ui.security

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.integrity.IntegrityStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SecurityUiState(
    val identityId: String = "",
    val hasIdentity: Boolean = false,
    val integrityStatus: IntegrityStatus? = null,
    val lastCheck: Long = 0L,
    val isChecking: Boolean = false,
    val identityCreated: Long = 0L,
    val keyStoreStatus: String = "Active"
)

class SecurityViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val securePrefs = com.chris.chipherlink.data.local.SecurePreferences(application)

    private val _uiState = MutableStateFlow(SecurityUiState())
    val uiState: StateFlow<SecurityUiState> = _uiState.asStateFlow()

    init {
        loadState()
    }

    private fun loadState() {
        _uiState.value = SecurityUiState(
            identityId = app.identityManager.getIdentityId() ?: "N/A",
            hasIdentity = app.identityManager.hasIdentity(),
            lastCheck = securePrefs.lastIntegrityCheckValue,
            keyStoreStatus = if (app.identityManager.hasIdentity()) "Active" else "Not initialized"
        )
    }

    fun verifyIntegrity() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChecking = true)
            val status = withContext(Dispatchers.IO) {
                app.integrityManager.verifyIntegrity()
            }
            securePrefs.lastIntegrityCheckValue = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                integrityStatus = status,
                lastCheck = System.currentTimeMillis(),
                isChecking = false
            )
        }
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
}
