package com.chris.chipherlink.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.data.local.ChatEntity
import com.chris.chipherlink.data.local.DeliveryStatus
import com.chris.chipherlink.data.local.MessageEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val authRepo = app.authRepository
    private val chatRepo = app.chatRepository

    val userId: String = authRepo.getCurrentUserId() ?: ""

    private val _chatId = MutableStateFlow("")

    val messages: StateFlow<List<MessageEntity>> = _chatId
        .flatMapLatest { id ->
            if (id.isNotEmpty()) chatRepo.getMessagesByChatId(id)
            else kotlinx.coroutines.flow.flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _chatName = MutableStateFlow("")
    val chatName: StateFlow<String> = _chatName

    private val _isTyping = MutableStateFlow(false)
    val isTyping: StateFlow<Boolean> = _isTyping

    fun init(chatId: String) {
        _chatId.value = chatId
        viewModelScope.launch {
            val chat: ChatEntity? = chatRepo.getChatById(chatId)
            _chatName.value = chat?.name ?: "Chat"
            // Mark messages as read when opening chat
            chatRepo.markAsRead(chatId)
        }
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            val result = chatRepo.sendMessage(_chatId.value, userId, content)
            result.onSuccess { messageId ->
                // Simulate delivery status progression
                simulateDeliveryProgression(messageId)
            }
        }
    }

    private fun simulateDeliveryProgression(messageId: String) {
        viewModelScope.launch {
            // SENT -> DELIVERED after 1 second (simulated)
            delay(1000)
            chatRepo.updateDeliveryStatus(messageId, DeliveryStatus.DELIVERED)

            // DELIVERED -> READ after 3 seconds (simulated)
            delay(2000)
            chatRepo.updateDeliveryStatus(messageId, DeliveryStatus.READ)
        }
    }

    fun getDeliveryStatus(message: MessageEntity): DeliveryStatus {
        return try {
            DeliveryStatus.valueOf(message.deliveryStatus)
        } catch (_: Exception) {
            DeliveryStatus.SENT
        }
    }
}
