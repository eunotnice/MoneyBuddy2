package com.example.moneybuddy2.data.remote

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseStorageService {

    private const val TAG = "FirebaseStorageService"
    private val storage = FirebaseStorage.getInstance()

    suspend fun uploadReceiptImage(
        uid: String,
        imageUri: Uri,
        context: Context
    ): String? {
        return try {
            val fileName = "${UUID.randomUUID()}.jpg"
            val path = "receipts/$uid/$fileName"
            val ref = storage.reference.child(path)

            Log.d(TAG, "Starting upload")
            Log.d(TAG, "UID: $uid")
            Log.d(TAG, "URI: $imageUri")
            Log.d(TAG, "Storage path: $path")

            context.contentResolver.openInputStream(imageUri).use { stream ->
                if (stream == null) {
                    Log.e(TAG, "openInputStream returned null")
                    return null
                }

                ref.putStream(stream).await()
            }

            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "Upload success. Download URL: $downloadUrl")

            downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Upload failed: ${e.message}", e)
            null
        }
    }

    suspend fun deleteReceiptImage(downloadUrl: String?): Boolean {
        if (downloadUrl == null) return true
        return try {
            val ref = FirebaseStorage.getInstance().getReferenceFromUrl(downloadUrl)
            ref.delete().await()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Delete failed: ${e.message}", e)
            false
        }
    }
}