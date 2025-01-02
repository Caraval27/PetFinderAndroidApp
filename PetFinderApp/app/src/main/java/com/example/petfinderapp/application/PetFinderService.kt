package com.example.petfinderapp.application

import android.net.Uri
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.infrastructure.RealtimeDbRepository
import com.example.petfinderapp.infrastructure.StorageRepository

class PetFinderService {
    private val realtimeDbRepository : RealtimeDbRepository = RealtimeDbRepository()
    private val storageRepository : StorageRepository = StorageRepository()

    suspend fun createPost(post : Post) {
        val downloadUris : MutableList<String> = mutableListOf()
        for (index in post.images.indices) {
            val downloadUri = storageRepository.uploadImage(post.images[index])
            if (downloadUri != null) {
                downloadUris.add(downloadUri)
            }
        }
        post.images = downloadUris
        realtimeDbRepository.insertPost(post)
    }

}