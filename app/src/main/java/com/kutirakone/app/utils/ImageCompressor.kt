package com.kutirakone.app.utils

import android.content.Context
import android.net.Uri
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.default
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ImageCompressor {
    suspend fun compressImage(context: Context, uri: Uri, maxSizeKb: Int = Constants.MAX_IMAGE_SIZE_KB): Uri {
        return withContext(Dispatchers.IO) {
            val file = getFileFromUri(context, uri) ?: return@withContext uri
            val compressedImage = Compressor.compress(context, file) {
                default() // Just use default compressor settings for now, zelory manages quality well
            }
            Uri.fromFile(compressedImage)
        }
    }

    private fun getFileFromUri(context: Context, uri: Uri): File? {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val tempFile = File.createTempFile("upload", ".jpg", context.cacheDir)
        tempFile.outputStream().use { outputStream ->
            inputStream.copyTo(outputStream)
        }
        return tempFile
    }
}
