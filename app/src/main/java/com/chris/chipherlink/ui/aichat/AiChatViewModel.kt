package com.chris.chipherlink.ui.aichat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.ai.AiMessage
import com.chris.chipherlink.ai.AiProviderManager
import com.chris.chipherlink.ai.MessageRole
import com.chris.chipherlink.data.local.AiMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AiChatUiState(
    val chatId: String? = null,
    val chatTitle: String = "",
    val messages: List<AiMessageEntity> = emptyList(),
    val isGenerating: Boolean = false,
    val currentResponse: String = "",
    val error: String? = null,
    val mode: String = "general"
)

class AiChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val aiRepo = app.aiRepository
    private val providerManager = app.aiProviderManager

    private val _uiState = MutableStateFlow(AiChatUiState())
    val uiState: StateFlow<AiChatUiState> = _uiState.asStateFlow()

    private var chatId: String? = null

    fun init(existingChatId: String? = null, mode: String = "general") {
        chatId = existingChatId
        _uiState.value = _uiState.value.copy(mode = mode)

        if (existingChatId != null) {
            viewModelScope.launch {
                aiRepo.getMessages(existingChatId).collect { messages ->
                    _uiState.value = _uiState.value.copy(messages = messages)
                }
            }
        }
    }

    fun sendMessage(content: String) {
        val currentChatId = chatId

        viewModelScope.launch {
            // Create chat if needed
            if (currentChatId == null) {
                val title = content.take(50) + if (content.length > 50) "..." else ""
                val newChatId = withContext(Dispatchers.IO) {
                    aiRepo.createChat(title, _uiState.value.mode)
                }
                chatId = newChatId
                _uiState.value = _uiState.value.copy(chatId = newChatId, chatTitle = title)

                // Start observing messages
                aiRepo.getMessages(newChatId).collect { messages ->
                    _uiState.value = _uiState.value.copy(messages = messages)
                }
            }

            // Save user message
            val targetChatId = chatId ?: return@launch
            withContext(Dispatchers.IO) {
                aiRepo.sendUserMessage(targetChatId, content)
            }

            // Generate AI response
            _uiState.value = _uiState.value.copy(isGenerating = true, currentResponse = "")

            try {
                val messages = withContext(Dispatchers.IO) {
                    aiRepo.getMessagesList(targetChatId).map {
                        AiMessage(
                            role = if (it.role == "user") MessageRole.USER else MessageRole.ASSISTANT,
                            content = it.content
                        )
                    }
                }

                val systemPrompt = if (_uiState.value.mode == "assistant") {
                    CIPHERLINK_ASSISTANT_PROMPT
                } else {
                    null
                }

                val responseBuilder = StringBuilder()
                providerManager.sendMessage(messages, systemPrompt).collect { chunk ->
                    responseBuilder.append(chunk)
                    _uiState.value = _uiState.value.copy(currentResponse = responseBuilder.toString())
                }

                // Save assistant response
                val fullResponse = responseBuilder.toString()
                if (fullResponse.isNotBlank()) {
                    withContext(Dispatchers.IO) {
                        aiRepo.saveAssistantMessage(targetChatId, fullResponse)
                    }
                }

                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    currentResponse = ""
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isGenerating = false,
                    error = "Failed to generate response: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        private const val CIPHERLINK_ASSISTANT_PROMPT = """You are CipherAI, the intelligent assistant built into CipherLink.

Your roles:
1. General chatbot - Answer questions about any topic
2. CipherLink assistant - Help users with app features, security, and settings

CipherLink Features:
- Secure messaging with local-first architecture
- Identity system with RSA-2048 key pairs
- SQLCipher encrypted database
- HMAC-SHA256 file integrity verification
- AES-256-GCM encryption via Android Keystore
- Recovery package system
- Customizable themes and backgrounds

Security Guidelines:
- Never encourage users to share sensitive data
- Explain security features clearly
- Recommend strong passwords
- Help users understand privacy settings

Be helpful, concise, and security-conscious."""
    }
}
