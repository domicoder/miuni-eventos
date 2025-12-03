package com.domicoder.miunieventos.data.remote

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageStorageDataSource @Inject constructor(
    private val storage: FirebaseStorage
) {
    companion object {
        private const val TAG = "ImageStorageDataSource"
        private const val EVENTS_IMAGES_PATH = "events"
        private const val MAX_FILE_SIZE_MB = 5
        private const val MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024L
    }

    suspend fun uploadEventImage(imageUri: Uri, eventId: String): Result<String> {
        return try {
            val fileName = "${eventId}_${UUID.randomUUID()}.jpg"
            val imageRef = storage.reference.child("$EVENTS_IMAGES_PATH/$fileName")
            
            imageRef.putFile(imageUri).await()
            val downloadUrl = imageRef.downloadUrl.await().toString()
            
            Log.d(TAG, "Image uploaded successfully: $downloadUrl")
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading image", e)
            Result.failure(e)
        }
    }

    suspend fun deleteEventImage(imageUrl: String): Result<Unit> {
        return try {
            val imageRef = storage.getReferenceFromUrl(imageUrl)
            imageRef.delete().await()
            Log.d(TAG, "Image deleted successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting image", e)
            Result.failure(e)
        }
    }
}

