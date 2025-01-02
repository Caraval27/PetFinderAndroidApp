package com.example.petfinderapp.presentation.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import kotlinx.coroutines.launch

class PetFinderVM : ViewModel() {
    private val posts = mutableListOf<Post>()
    private val petFinderService : PetFinderService = PetFinderService()
    var searchImages = mutableStateOf<List<String>>(emptyList())
        private set

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
                title, animalType, race, color, userName, phoneNumber, description, postType, images
            )
        viewModelScope.launch {
            petFinderService.createPost(post)
        }
    }

    fun updateSearchImages(newImages: List<String>) {
        searchImages.value = newImages
    }
}