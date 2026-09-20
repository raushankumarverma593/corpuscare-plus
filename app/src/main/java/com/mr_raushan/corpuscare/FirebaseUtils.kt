package com.mr_raushan.corpuscare

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

object FirebaseUtils {
    suspend fun uploadImage(uri: Uri, folder: String): String {
        return try {
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = UUID.randomUUID().toString() + ".jpg"
            val imageRef = storageRef.child("$folder/$fileName")
            
            imageRef.putFile(uri).await()
            imageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}
