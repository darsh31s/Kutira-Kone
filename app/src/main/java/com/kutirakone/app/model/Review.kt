package com.kutirakone.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class Review(
    val id: String = "",
    val vendorId: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val listingId: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "vendorId" to vendorId,
            "customerId" to customerId,
            "customerName" to customerName,
            "listingId" to listingId,
            "rating" to rating,
            "comment" to comment,
            "createdAt" to createdAt
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): Review? {
            if (!doc.exists()) return null
            return Review(
                id = doc.getString("id") ?: doc.id,
                vendorId = doc.getString("vendorId") ?: "",
                customerId = doc.getString("customerId") ?: "",
                customerName = doc.getString("customerName") ?: "",
                listingId = doc.getString("listingId") ?: "",
                rating = doc.getLong("rating")?.toInt() ?: 5,
                comment = doc.getString("comment") ?: "",
                createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now()
            )
        }
    }
}
