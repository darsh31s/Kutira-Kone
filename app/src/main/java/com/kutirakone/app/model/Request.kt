package com.kutirakone.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.kutirakone.app.model.enums.RequestStatus

data class Request(
    val id: String = "",
    val listingId: String = "",
    val listingMaterial: String = "",
    val requesterId: String = "",
    val requesterName: String = "",
    val ownerId: String = "",
    val type: String = "buy",
    val swapOffer: String = "",
    val status: RequestStatus = RequestStatus.PENDING,
    val createdAt: Timestamp = Timestamp.now(),
    val conversationId: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "listingId" to listingId,
            "listingMaterial" to listingMaterial,
            "requesterId" to requesterId,
            "requesterName" to requesterName,
            "ownerId" to ownerId,
            "type" to type,
            "swapOffer" to swapOffer,
            "status" to status.name,
            "createdAt" to createdAt,
            "conversationId" to conversationId
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): Request? {
            if (!doc.exists()) return null
            return Request(
                id = doc.getString("id") ?: doc.id,
                listingId = doc.getString("listingId") ?: "",
                listingMaterial = doc.getString("listingMaterial") ?: "",
                requesterId = doc.getString("requesterId") ?: "",
                requesterName = doc.getString("requesterName") ?: "",
                ownerId = doc.getString("ownerId") ?: "",
                type = doc.getString("type") ?: "buy",
                swapOffer = doc.getString("swapOffer") ?: "",
                status = runCatching { RequestStatus.valueOf(doc.getString("status") ?: "PENDING") }.getOrDefault(RequestStatus.PENDING),
                createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                conversationId = doc.getString("conversationId") ?: ""
            )
        }
    }
}
