package com.example.petfinderapp.presentation.viewModel

import androidx.lifecycle.ViewModel

class PetFinderVM : ViewModel() {
    private val posts = mutableListOf<Post>()

    fun createPost(
        title: String,
        animalType: String,
        race: String,
        color: String,
        username: String,
        phoneNumber: String,
        description: String,
        postType: Boolean,
        images: List<String>
    ) {
        posts.add(
            Post(
                title, animalType, race, color, username, phoneNumber, description, postType, images
            )
        )
    }
}

data class Post(
    val title: String,
    val animalType: String,
    val race: String,
    val color: String,
    val username: String,
    val phoneNumber: String,
    val description: String,
    val postType: Boolean, //true = found, false == looking
    val images: List<String>
)