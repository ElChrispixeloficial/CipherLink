package com.chris.chipherlink.ui.searchuser

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.chris.chipherlink.CipherLinkApplication
import com.chris.chipherlink.data.local.ContactEntity
import com.chris.chipherlink.data.local.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchUserUiState(
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val foundUser: UserEntity? = null,
    val isOwnId: Boolean = false,
    val isAlreadyContact: Boolean = false,
    val existingContact: ContactEntity? = null,
    val error: String? = null,
    val contactAdded: Boolean = false
)

class SearchUserViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as CipherLinkApplication

    private val _uiState = MutableStateFlow(SearchUserUiState())
    val uiState: StateFlow<SearchUserUiState> = _uiState.asStateFlow()

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query.uppercase(),
            error = null
        )
    }

    fun search() {
        val query = _uiState.value.searchQuery.trim()
        if (query.length < 3) {
            _uiState.value = _uiState.value.copy(error = "Enter a valid CipherLink ID (e.g. CL-7A91F3)")
            return
        }

        val normalized = if (query.startsWith("CL-")) query else "CL-$query"
        if (!com.chris.chipherlink.utils.CipherLinkIdGenerator.isValid(normalized)) {
            _uiState.value = _uiState.value.copy(error = "Invalid format. Use CL-XXXXXX")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, error = null)

            try {
                val userId = app.authRepository.getCurrentUserId()
                    ?: run {
                        _uiState.value = _uiState.value.copy(isSearching = false, error = "Not logged in")
                        return@launch
                    }

                // Check if it's own ID
                val isOwn = app.contactRepository.isOwnId(userId, normalized)
                if (isOwn) {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        foundUser = null,
                        isOwnId = true,
                        error = "That's your own CipherLink ID"
                    )
                    return@launch
                }

                // Check if already a contact
                val existingContact = app.contactRepository.findByCipherLinkId(normalized)
                if (existingContact != null) {
                    val user = app.contactRepository.findUserByCipherLinkId(normalized)
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        foundUser = user,
                        isOwnId = false,
                        isAlreadyContact = true,
                        existingContact = existingContact
                    )
                    return@launch
                }

                // Search in registered users
                val user = app.contactRepository.findUserByCipherLinkId(normalized)
                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        isSearching = false,
                        error = "User not found"
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    foundUser = user,
                    isOwnId = false,
                    isAlreadyContact = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSearching = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun addContact() {
        val user = _uiState.value.foundUser ?: return
        val userId = app.authRepository.getCurrentUserId() ?: return

        viewModelScope.launch {
            try {
                val profile = app.profileRepository.getProfile(user.id)
                app.contactRepository.addContact(
                    localUserId = userId,
                    targetUserId = user.id,
                    cipherLinkId = user.cipherLinkId,
                    displayName = profile?.displayName ?: user.username,
                    username = user.username,
                    photoPath = profile?.photoPath
                )
                _uiState.value = _uiState.value.copy(contactAdded = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Failed to add contact: ${e.message}")
            }
        }
    }

    fun clearResult() {
        _uiState.value = SearchUserUiState()
    }
}
