package com.example.petfinderapp.presentation.viewModel

import androidx.lifecycle.ViewModel
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType

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
        postType: PostType,
        images: List<String>
    ) {
        posts.add(
            Post(
                title, animalType, race, color, username, phoneNumber, description, postType, images
            )
        )
    }
}