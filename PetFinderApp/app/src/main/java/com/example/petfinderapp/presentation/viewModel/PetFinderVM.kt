package com.example.petfinderapp.presentation.viewModel

import android.net.Uri
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
    val posts: StateFlow<List<Post>> = petFinderService.posts
    private val _view = MutableStateFlow(View.Looking)
    val view: StateFlow<View>
        get() = _view

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

    fun changeView(view: View) {
        viewModelScope.launch {
            if (_view.value != View.Post) {
                petFinderService.stopStreamingPosts(PostType.valueOf(_view.value.toString()))
            }
            _view.value = view
            if (view != View.Post) {
                petFinderService.startStreamingPosts(PostType.valueOf(view.toString()))
            }
        }
    }
}

enum class View {
    Post, Looking, Found
}