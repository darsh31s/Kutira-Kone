package com.kutirakone.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.kutirakone.app.model.enums.ListingStatus
import com.kutirakone.app.model.enums.ListingType
import com.kutirakone.app.model.enums.MaterialType

data class Listing(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val userAvgRating: Double = 0.0,
    val material: MaterialType = MaterialType.COTTON,
    val sizeMetres: Double = 0.0,
    val colour: String = "",
    val condition: String = "",
    val type: ListingType = ListingType.SELL,
    val photoURLs: List<String> = emptyList(),
    val price: Double? = null,
    val swapOffer: String? = null,
    val geoHash: String = "",
    val location: GeoPoint? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp = Timestamp(Timestamp.now().seconds + 2592000, 0),
    val status: ListingStatus = ListingStatus.AVAILABLE,
    val distanceKm: Double = 0.0,
    val reservedFor: String = ""
) {
    val safePhotoURLs: List<String>
        get() = photoURLs.map { url ->
            if (url.startsWith("file:///data/") || (url.startsWith("file:/") && url.contains("com.kutirakone.app"))) {
                url
            } else if (url.startsWith("content://") || url.startsWith("file:/") || !url.startsWith("http")) {
                when (material) {
                    MaterialType.SILK -> "https://images.unsplash.com/photo-1582201942988-13e60e4556ee?auto=format&fit=crop&w=600&q=80"
                    MaterialType.COTTON -> "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=600&q=80"
                    MaterialType.WOOL -> "https://images.unsplash.com/photo-1606136920197-00994bf5c021?auto=format&fit=crop&w=600&q=80"
                    MaterialType.SYNTHETIC -> "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=600&q=80"
                    MaterialType.BLEND -> "https://images.unsplash.com/photo-1571210862729-78a52d3779a2?auto=format&fit=crop&w=600&q=80"
                    MaterialType.JUTE -> "https://images.unsplash.com/photo-1590736969955-71cc94801759?auto=format&fit=crop&w=600&q=80"
                }
            } else {
                url
            }
        }.ifEmpty {
            listOf(
                when (material) {
                    MaterialType.SILK -> "https://images.unsplash.com/photo-1582201942988-13e60e4556ee?auto=format&fit=crop&w=600&q=80"
                    MaterialType.COTTON -> "https://images.unsplash.com/photo-1544816155-12df9643f363?auto=format&fit=crop&w=600&q=80"
                    MaterialType.WOOL -> "https://images.unsplash.com/photo-1606136920197-00994bf5c021?auto=format&fit=crop&w=600&q=80"
                    MaterialType.SYNTHETIC -> "https://images.unsplash.com/photo-1523381210434-271e8be1f52b?auto=format&fit=crop&w=600&q=80"
                    MaterialType.BLEND -> "https://images.unsplash.com/photo-1571210862729-78a52d3779a2?auto=format&fit=crop&w=600&q=80"
                    MaterialType.JUTE -> "https://images.unsplash.com/photo-1590736969955-71cc94801759?auto=format&fit=crop&w=600&q=80"
                }
            )
        }

    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "userId" to userId,
            "userName" to userName,
            "userAvgRating" to userAvgRating,
            "material" to material.name,
            "sizeMetres" to sizeMetres,
            "colour" to colour,
            "condition" to condition,
            "type" to type.name,
            "photoURLs" to photoURLs,
            "price" to price,
            "swapOffer" to swapOffer,
            "geoHash" to geoHash,
            "location" to location,
            "createdAt" to createdAt,
            "expiresAt" to expiresAt,
            "status" to status.name,
            "reservedFor" to reservedFor
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): Listing? {
            if (!doc.exists()) return null
            return Listing(
                id = doc.getString("id") ?: doc.id,
                userId = doc.getString("userId") ?: "",
                userName = doc.getString("userName") ?: "",
                userAvgRating = doc.getDouble("userAvgRating") ?: 0.0,
                material = runCatching { MaterialType.valueOf(doc.getString("material") ?: "COTTON") }.getOrDefault(MaterialType.COTTON),
                sizeMetres = doc.getDouble("sizeMetres") ?: 0.0,
                colour = doc.getString("colour") ?: "",
                condition = doc.getString("condition") ?: "",
                type = runCatching { ListingType.valueOf(doc.getString("type") ?: "SELL") }.getOrDefault(ListingType.SELL),
                photoURLs = (doc.get("photoURLs") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                price = doc.getDouble("price"),
                swapOffer = doc.getString("swapOffer"),
                geoHash = doc.getString("geoHash") ?: "",
                location = doc.getGeoPoint("location"),
                createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                expiresAt = doc.getTimestamp("expiresAt") ?: Timestamp(Timestamp.now().seconds + 2592000, 0),
                status = runCatching { ListingStatus.valueOf(doc.getString("status") ?: "AVAILABLE") }.getOrDefault(ListingStatus.AVAILABLE),
                distanceKm = 0.0, // Computed client side
                reservedFor = doc.getString("reservedFor") ?: ""
            )
        }
    }
}
