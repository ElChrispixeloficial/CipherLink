package com.chris.chipherlink.ui.aichat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.data.local.AiChatEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AiChatListUiState(
    val chats: List<AiChatEntity> = emptyList(),
    val isLoading: Boolean = true
)

class AiChatListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val aiRepo = app.aiRepository

    private val _uiState = MutableStateFlow(AiChatListUiState())
    val uiState: StateFlow<AiChatListUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            aiRepo.getAllChats().collect { chats ->
                _uiState.value = _uiState.value.copy(
                    chats = chats,
                    isLoading = false
                )
            }
        }
    }

    fun deleteChat(chatId: String) {
        viewModelScope.launch {
            aiRepo.deleteChat(chatId)
        }
    }

    fun createNewChat(): String {
        // Return empty string to indicate new chat should be created on message send
        return ""
    }
}
