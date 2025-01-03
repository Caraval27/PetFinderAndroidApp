package com.example.petfinderapp.presentation.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PetFinderVM : ViewModel() {
    private val petFinderService : PetFinderService = PetFinderService()
    var searchImages = mutableStateOf<List<String>>(emptyList())
        private set
    val posts: StateFlow<List<Post>> = petFinderService.posts
    private var _postType = PostType.Looking

    fun createPost(
        title: String,
        animalType: String,
        race: String,
        color: String,
        userName: String,
        phoneNumber: String,
        description: String,
        postType: PostType,
        images: List<String>
    ) {
        val post =
            Post(
                title = title,
                animalType = animalType,
                race = race,
                color = color,
                userName = userName,
                phoneNumber = phoneNumber,
                description = description,
                postType = postType,
                images = images
            )
        viewModelScope.launch {
            petFinderService.createPost(post)
        }
    }

    fun initFeed(postType: PostType) {
        viewModelScope.launch {
            petFinderService.stopStreamingPosts(_postType)
            _postType = postType
            petFinderService.startStreamingPosts(postType)
        }
    }

    fun updateSearchImages(newImages: List<String>) {
        searchImages.value = newImages
    }
}