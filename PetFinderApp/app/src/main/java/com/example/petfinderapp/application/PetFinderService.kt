package com.example.petfinderapp.application

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.domain.SubSubcategory
import com.example.petfinderapp.domain.Subcategory
import com.example.petfinderapp.infrastructure.RealtimeDbRepository
import com.example.petfinderapp.infrastructure.StorageRepository
import kotlinx.coroutines.flow.StateFlow

class PetFinderService(
    val applicationContext: Context
){
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

    fun hasInternetConnection() : Boolean {
        val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java)
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun loadCategories(context: Context): List<Category> {
        val categories = mutableListOf<Category>()
        val files = context.assets.list("") ?: emptyArray()

        for (fileName in files) {
            if (fileName.startsWith("Category") && fileName.endsWith(".txt")) {
                val lines = context.assets.open(fileName).bufferedReader().use { it.readLines() }
                if (lines.isNotEmpty()) {
                    val categoryName = lines[0]
                    val subcategories = lines.drop(1).map { subcategoryName ->
                        if (subcategoryName == "Dog" || subcategoryName == "Cat") {
                            val breedsFileName = when (subcategoryName) {
                                "Dog" -> "SubcategoryDogBreeds.txt"
                                "Cat" -> "SubcategoryCatBreeds.txt"
                                else -> null
                            }

                            val breeds = breedsFileName?.let { breedFile ->
                                context.assets.open(breedFile).bufferedReader().useLines { lines ->
                                    lines.map { SubSubcategory(name = it) }.toList()
                                }
                            } ?: emptyList()

                            Subcategory(name = subcategoryName, isSelected = false, subcategories = breeds)
                        } else {
                            Subcategory(name = subcategoryName)
                        }
                    }
                    categories.add(Category(name = categoryName, subcategories = subcategories))
                }
            }
        }

        return categories
    }
}