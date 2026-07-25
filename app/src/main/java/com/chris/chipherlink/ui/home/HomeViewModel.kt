package com.chris.chipherlink.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.data.local.ChatEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication
    private val authRepo = app.authRepository
    private val chatRepo = app.chatRepository

    val userId: String = authRepo.getCurrentUserId() ?: ""

    val chats: StateFlow<List<ChatEntity>> = chatRepo.getChatsByUserId(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun logout() {
        viewModelScope.launch {
            authRepo.logout()
        }
    }
}
