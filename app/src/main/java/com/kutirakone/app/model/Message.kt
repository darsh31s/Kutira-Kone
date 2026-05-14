package com.kutirakone.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class Message(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now(),
    val isRead: Boolean = false
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "senderId" to senderId,
            "senderName" to senderName,
            "text" to text,
            "timestamp" to timestamp,
            "isRead" to isRead
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): Message? {
            if (!doc.exists()) return null
            return Message(
                id = doc.getString("id") ?: doc.id,
                senderId = doc.getString("senderId") ?: "",
                senderName = doc.getString("senderName") ?: "",
                text = doc.getString("text") ?: "",
                timestamp = doc.getTimestamp("timestamp") ?: Timestamp.now(),
                isRead = doc.getBoolean("isRead") ?: false
            )
        }
    }
}
