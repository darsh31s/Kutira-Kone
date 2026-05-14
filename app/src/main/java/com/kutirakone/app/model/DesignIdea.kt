package com.kutirakone.app.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.serialization.Serializable

@Serializable
data class DesignIdea(
    val title: String = "",
    val difficulty: String = "Easy",
    val description: String = ""
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "difficulty" to difficulty,
            "description" to description
        )
    }
    
    companion object {
        fun fromMap(map: Map<String, Any?>): DesignIdea {
            return DesignIdea(
                title = map["title"] as? String ?: "",
                difficulty = map["difficulty"] as? String ?: "Easy",
                description = map["description"] as? String ?: ""
            )
        }
    }
}

data class ListingDesignIdeas(
    val id: String = "",
    val listingId: String = "",
    val ideas: List<DesignIdea> = emptyList(),
    val generatedAt: Timestamp = Timestamp.now()
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "listingId" to listingId,
            "ideas" to ideas.map { it.toMap() },
            "generatedAt" to generatedAt
        )
    }

    companion object {
        fun fromDocument(doc: DocumentSnapshot): ListingDesignIdeas? {
            if (!doc.exists()) return null
            val ideasList = doc.get("ideas") as? List<*>
            val parsedIdeas = ideasList?.filterIsInstance<Map<String, Any?>>()?.map { DesignIdea.fromMap(it) } ?: emptyList()
            return ListingDesignIdeas(
                id = doc.getString("id") ?: doc.id,
                listingId = doc.getString("listingId") ?: "",
                ideas = parsedIdeas,
                generatedAt = doc.getTimestamp("generatedAt") ?: Timestamp.now()
            )
        }
    }
}
