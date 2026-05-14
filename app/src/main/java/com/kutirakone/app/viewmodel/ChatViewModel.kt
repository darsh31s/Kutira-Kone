package com.kutirakone.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kutirakone.app.model.Message
import com.kutirakone.app.repository.ChatRepository
import com.kutirakone.app.ui.common.utils.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {

    private val _messages = MutableStateFlow<UiState<List<Message>>>(UiState.Idle)
    val messages: StateFlow<UiState<List<Message>>> = _messages.asStateFlow()

    val messageText = MutableStateFlow("")

    private val _sendState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val sendState: StateFlow<UiState<Unit>> = _sendState.asStateFlow()

    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    fun loadMessages(conversationId: String, currentUserId: String) {
        viewModelScope.launch {
            chatRepository.getMessages(conversationId).collect { state ->
                _messages.value = state
                if (state is UiState.Success) {
                    chatRepository.markMessagesRead(conversationId, currentUserId).collect {}
                }
            }
        }
    }

    fun sendMessage(conversationId: String, senderId: String, senderName: String) {
        val text = messageText.value.trim()
        if (text.isEmpty()) return
        
        val message = Message(
            senderId = senderId,
            senderName = senderName,
            text = text
        )

        viewModelScope.launch {
            chatRepository.sendMessage(conversationId, message).collect {
                _sendState.value = it
                if (it is UiState.Success) {
                    messageText.value = ""
                }
            }
        }
    }

    fun markAsComplete(conversationId: String, requestId: String, listingId: String) {
        // Normally this would update Firestore to set the request to COMPLETED and listing to COMPLETED
        // For now, we update local state to trigger navigation to review screen
        _isCompleted.value = true
    }

    fun clearMessageText() {
        messageText.value = ""
    }
}
