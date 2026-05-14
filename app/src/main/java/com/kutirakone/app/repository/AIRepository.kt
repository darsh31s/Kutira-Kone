package com.kutirakone.app.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.firestore.FirebaseFirestore
import com.kutirakone.app.BuildConfig
import com.kutirakone.app.model.DesignIdea
import com.kutirakone.app.model.ListingDesignIdeas
import com.kutirakone.app.ui.common.utils.UiState
import com.kutirakone.app.utils.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json

class AIRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    fun generateDesignIdeas(material: String, sizeMetres: Double, listingId: String? = null): Flow<UiState<List<DesignIdea>>> = flow {
        emit(UiState.Loading)
        if (BuildConfig.GEMINI_API_KEY.isEmpty() || BuildConfig.GEMINI_API_KEY == "null") {
            emit(UiState.Error("Gemini API Key is missing. Please add it to local.properties."))
            return@flow
        }
        
        val generativeModel = GenerativeModel(
            modelName = Constants.GEMINI_MODEL,
            apiKey = BuildConfig.GEMINI_API_KEY
        )

        try {
            // Check cache if listingId is provided
            if (listingId != null) {
                val cachedDoc = firestore.collection(Constants.DESIGN_IDEAS_COLLECTION).document(listingId).get().await()
                if (cachedDoc.exists()) {
                    val cachedIdeas = ListingDesignIdeas.fromDocument(cachedDoc)
                    if (cachedIdeas != null && cachedIdeas.ideas.isNotEmpty()) {
                        emit(UiState.Success(cachedIdeas.ideas))
                        return@flow
                    }
                }
            }

            val prompt = """
                You are a creative craft expert. A user has a piece of $material fabric that is $sizeMetres metres long.
                Suggest exactly ${Constants.AI_IDEA_COUNT} DIY craft project ideas.
                Return ONLY a valid JSON array with this exact structure:
                [
                  {
                    "title": "Project Name",
                    "difficulty": "Easy",
                    "description": "2 sentence description."
                  }
                ]
                difficulty must be one of: Easy, Medium, Hard.
                No extra text. No markdown. Only the JSON array.
            """.trimIndent()

            val response = generativeModel.generateContent(
                content { text(prompt) }
            )
            
            val responseText = response.text ?: throw Exception("Empty response from AI")
            // Clean up any potential markdown formatting the AI might still include
            val jsonString = responseText.replace("```json", "").replace("```", "").trim()
            
            val json = Json { ignoreUnknownKeys = true }
            val ideas = json.decodeFromString<List<DesignIdea>>(jsonString)

            // Cache if listingId is provided
            if (listingId != null) {
                val listingIdeas = ListingDesignIdeas(
                    id = listingId,
                    listingId = listingId,
                    ideas = ideas
                )
                firestore.collection(Constants.DESIGN_IDEAS_COLLECTION).document(listingId).set(listingIdeas.toMap()).await()
            }

            emit(UiState.Success(ideas))

        } catch (e: Exception) {
            val errorMsg = e.message ?: "Failed to generate design ideas"
            if (errorMsg.contains("PERMISSION_DENIED") && errorMsg.contains("unregistered callers")) {
                emit(UiState.Error("Gemini API Key is missing. Please add it to local.properties and Rebuild."))
            } else if (errorMsg.contains("MissingFieldException")) {
                emit(UiState.Error("API Key is valid, but Generative Language API is not enabled. Create a key at aistudio.google.com"))
            } else {
                emit(UiState.Error("API Error: $errorMsg"))
            }
        }
    }
}
