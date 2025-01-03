package com.example.petfinderapp.presentation.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class PetFinderVM : ViewModel() {
    private val petFinderService : PetFinderService = PetFinderService()
    var searchImages = mutableStateOf<List<String>>(emptyList())
        private set
    val posts: StateFlow<List<Post>> = petFinderService.posts
    val post: StateFlow<Post> = petFinderService.post
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
                date = LocalDateTime.now().toString(),
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
        if (postType != _postType) {
            viewModelScope.launch {
                petFinderService.stopStreamingPostFeed(_postType)
                _postType = postType
                petFinderService.startStreamingPostFeed(postType)
            }
        }
    }

    fun initDetails(postId: String) {
        if (postId != post.value.id) {
            viewModelScope.launch {
                petFinderService.startStreamingPostDetails(postId)
            }
        }
    }

    fun updateSearchImages(newImages: List<String>) {
        searchImages.value = newImages
    }
}