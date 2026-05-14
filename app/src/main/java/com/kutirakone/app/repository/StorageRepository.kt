package com.kutirakone.app.repository

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.kutirakone.app.ui.common.utils.UiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class StorageRepository(private val storage: FirebaseStorage = FirebaseStorage.getInstance()) {

    fun uploadListingImage(context: android.content.Context, userId: String, inputStream: java.io.InputStream, listingId: String): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        try {
            val directory = context.getDir("listing_photos", android.content.Context.MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val filename = "listing_${userId}_${listingId}_${UUID.randomUUID()}.jpg"
            val localFile = java.io.File(directory, filename)
            
            java.io.FileOutputStream(localFile).use { output ->
                inputStream.copyTo(output)
            }
            
            val localUrl = Uri.fromFile(localFile).toString()
            emit(UiState.Success(localUrl))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to save image locally"))
        } finally {
            try {
                inputStream.close()
            } catch (ex: Exception) {
                // ignore
            }
        }
    }

    fun uploadProfileImage(context: android.content.Context, userId: String, imageUri: Uri): Flow<UiState<String>> = flow {
        emit(UiState.Loading)
        try {
            val inputStream = context.contentResolver.openInputStream(imageUri)
            if (inputStream == null) {
                emit(UiState.Error("Could not open profile image"))
                return@flow
            }
            val directory = context.getDir("profile_photos", android.content.Context.MODE_PRIVATE)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val filename = "profile_${userId}_${UUID.randomUUID()}.jpg"
            val localFile = java.io.File(directory, filename)
            
            java.io.FileOutputStream(localFile).use { output ->
                inputStream.copyTo(output)
            }
            
            val localUrl = Uri.fromFile(localFile).toString()
            emit(UiState.Success(localUrl))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to save profile image locally"))
        }
    }

    fun deleteImage(imageUrl: String): Flow<UiState<Unit>> = flow {
        emit(UiState.Loading)
        try {
            if (imageUrl.startsWith("file:/")) {
                val uri = Uri.parse(imageUrl)
                val file = uri.path?.let { java.io.File(it) }
                if (file != null && file.exists()) {
                    file.delete()
                }
            } else {
                val ref = storage.getReferenceFromUrl(imageUrl)
                ref.delete().await()
            }
            emit(UiState.Success(Unit))
        } catch (e: Exception) {
            emit(UiState.Error(e.message ?: "Failed to delete image"))
        }
    }
}
