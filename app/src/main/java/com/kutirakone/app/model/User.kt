package com.kutirakone.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.kutirakone.app.model.enums.UserRole

data class User(
    val uid: String = "",
    val name: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.ARTISAN,
    val village: String = "",
    val location: GeoPoint? = null,
    val avgRating: Double = 0.0,
    val listingCount: Int = 0,
    val profilePhotoURL: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val fcmToken: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "uid" to uid,
            "name" to name,
            "phone" to phone,
            "role" to role.name,
            "village" to village,
            "location" to location,
            "avgRating" to avgRating,
            "listingCount" to listingCount,
            "profilePhotoURL" to profilePhotoURL,
            "createdAt" to createdAt,
            "fcmToken" to fcmToken
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): User? {
            if (!doc.exists()) return null
            return User(
                uid = doc.getString("uid") ?: doc.id,
                name = doc.getString("name") ?: "",
                phone = doc.getString("phone") ?: "",
                role = runCatching { UserRole.valueOf(doc.getString("role") ?: "ARTISAN") }.getOrDefault(UserRole.ARTISAN),
                village = doc.getString("village") ?: "",
                location = doc.getGeoPoint("location"),
                avgRating = doc.getDouble("avgRating") ?: 0.0,
                listingCount = doc.getLong("listingCount")?.toInt() ?: 0,
                profilePhotoURL = doc.getString("profilePhotoURL") ?: "",
                createdAt = doc.getTimestamp("createdAt") ?: Timestamp.now(),
                fcmToken = doc.getString("fcmToken") ?: ""
            )
        }
    }
}
