package com.example.petfinderapp.infrastructure

import android.net.Uri
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await

class StorageRepository {
    private val storageRef = Firebase.storage.getReference()

    suspend fun uploadImage(imageUriString : String) : String? {
        val imageUri : Uri = Uri.parse(imageUriString)
        val imageRef = imageUri.lastPathSegment?.let { storageRef.child(it) }
        if (imageRef == null) {
            return null;
        }
        val task = imageRef.putFile(imageUri)
        task.addOnSuccessListener {
            Log.d("StorageRepository", "Succeeded to upload image")
        }
        task.addOnFailureListener {
            Log.e("StorageRepository", "Failed to upload image", it)
        }
        task.await()
        return imageRef.downloadUrl.await().toString()
    }
}