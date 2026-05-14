package com.kutirakone.app.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.kutirakone.app.model.Message
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun sendMessage(conversationId: String, message: Message): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val docRef = firestore.collection(Constants.MESSAGES_COLLECTION).document(conversationId)
                .collection("msgs").document()
            val msgWithId = message.copy(id = docRef.id)
            docRef.set(msgWithId.toMap()).await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to send message"))
        }
    }

    fun getMessages(conversationId: String): Flow<UiState<List<Message>>> = callbackFlow {
        trySend(UiState.Loading)
        val listener = firestore.collection(Constants.MESSAGES_COLLECTION).document(conversationId)
            .collection("msgs")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(UiState.Error(error.message ?: "Failed to get messages"))
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.documents.mapNotNull { Message.fromDocument(it) }
                    trySend(UiState.Success(messages))
                }
            }
        awaitClose { listener.remove() }
    }

    fun markMessagesRead(conversationId: String, currentUserId: String): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            val unreadMsgs = firestore.collection(Constants.MESSAGES_COLLECTION).document(conversationId)
                .collection("msgs")
                .whereEqualTo("isRead", false)
                .get().await()
                
            val batch = firestore.batch()
            unreadMsgs.documents.forEach { doc ->
                if (doc.getString("senderId") != currentUserId) {
                    batch.update(doc.reference, "isRead", true)
                }
            }
            batch.commit().await()
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to mark read"))
        }
    }

    fun createConversation(requestId: String, participants: List<String>): String {
        // Can be a hash or combination of participant IDs + requestId
        // For simplicity using a UUID but saving participants could be done in a separate document
        return "conv_${requestId}_${UUID.randomUUID().toString().take(8)}"
    }
}
