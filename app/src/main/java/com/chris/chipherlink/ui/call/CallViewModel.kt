package com.chris.chipherlink.ui.call

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Call state for the beta calling system.
 */
enum class CallState {
    IDLE,
    CALLING,        // Initiating call
    RINGING,        // Receiving call
    CONNECTING,     // Establishing connection
    IN_CALL,        // Active call
    ENDING,         // Hanging up
    ENDED,          // Call finished
    FAILED          // Call failed
}

data class CallInfo(
    val chatId: String,
    val contactName: String,
    val state: CallState = CallState.IDLE,
    val duration: Long = 0,        // seconds
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val errorMessage: String? = null
)

class CallViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication

    private val _callInfo = MutableStateFlow(CallInfo("", ""))
    val callInfo: StateFlow<CallInfo> = _callInfo.asStateFlow()

    private var durationTimer: kotlinx.coroutines.Job? = null

    fun initiateCall(chatId: String, contactName: String) {
        _callInfo.value = CallInfo(
            chatId = chatId,
            contactName = contactName,
            state = CallState.CALLING
        )

        viewModelScope.launch {
            // Simulate call establishment
            delay(1500)
            _callInfo.value = _callInfo.value.copy(state = CallState.CONNECTING)
            delay(1000)
            _callInfo.value = _callInfo.value.copy(state = CallState.IN_CALL)
            startDurationTimer()
        }
    }

    fun answerCall(chatId: String, contactName: String) {
        _callInfo.value = CallInfo(
            chatId = chatId,
            contactName = contactName,
            state = CallState.IN_CALL
        )
        startDurationTimer()
    }

    fun endCall() {
        durationTimer?.cancel()
        _callInfo.value = _callInfo.value.copy(state = CallState.ENDING)

        viewModelScope.launch {
            delay(500)
            _callInfo.value = _callInfo.value.copy(
                state = CallState.ENDED,
                duration = _callInfo.value.duration
            )
        }
    }

    fun toggleMute() {
        _callInfo.value = _callInfo.value.copy(
            isMuted = !_callInfo.value.isMuted
        )
    }

    fun toggleSpeaker() {
        _callInfo.value = _callInfo.value.copy(
            isSpeakerOn = !_callInfo.value.isSpeakerOn
        )
    }

    fun reset() {
        durationTimer?.cancel()
        _callInfo.value = CallInfo("", "")
    }

    private fun startDurationTimer() {
        durationTimer?.cancel()
        durationTimer = viewModelScope.launch {
            var seconds = 0L
            while (true) {
                delay(1000)
                seconds++
                _callInfo.value = _callInfo.value.copy(duration = seconds)
            }
        }
    }

    fun formatDuration(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(mins, secs)
    }
}
