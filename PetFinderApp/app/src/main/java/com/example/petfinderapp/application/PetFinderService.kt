package com.example.petfinderapp.application

import android.content.Context
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.domain.Subcategory
import com.example.petfinderapp.infrastructure.RealtimeDbRepository
import com.example.petfinderapp.infrastructure.StorageRepository
import kotlinx.coroutines.flow.StateFlow

class PetFinderService{
    private val realtimeDbRepository : RealtimeDbRepository = RealtimeDbRepository()
    private val storageRepository : StorageRepository = StorageRepository()

    val posts: StateFlow<List<Post>> = realtimeDbRepository.posts
    val post: StateFlow<Post> = realtimeDbRepository.post

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

    fun startStreamingPostFeed(postType: PostType) {
        realtimeDbRepository.addPostFeedListener(postType)
    }

    fun stopStreamingPostFeed(postType: PostType) {
        realtimeDbRepository.removePostFeedListener(postType)
    }

    fun startStreamingPostDetails(postId: String) {
        realtimeDbRepository.addPostDetailsListener(postId)
    }

    fun loadCategories(context: Context): List<Category> {
        val categories = mutableListOf<Category>()

        val files = context.assets.list("") ?: emptyArray()

        for (fileName in files) {
            if (fileName.startsWith("Category") && fileName.endsWith(".txt")) {
                val lines = context.assets.open(fileName).bufferedReader().use { it.readLines() }
                if (lines.isNotEmpty()) {
                    val categoryName = lines[0]
                    val subcategories = lines.drop(1).map { Subcategory(name = it) }
                    categories.add(Category(name = categoryName, subcategories = subcategories))
                }
            }
        }

        return categories
    }
}