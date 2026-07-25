package com.chris.chipherlink.ui.createchat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateChatUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val chatCreatedId: String? = null
)

class CreateChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val chatRepo = app.chatRepository
    private val authRepo = app.authRepository

    private val _uiState = MutableStateFlow(CreateChatUiState())
    val uiState: StateFlow<CreateChatUiState> = _uiState.asStateFlow()

    fun createChat(name: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            val userId = authRepo.getCurrentUserId() ?: return@launch
            val result = chatRepo.createChat(name, userId)
            result.fold(
                onSuccess = { chatId ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        chatCreatedId = chatId
                    )
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to create chat"
                    )
                }
            )
        }
    }
}
