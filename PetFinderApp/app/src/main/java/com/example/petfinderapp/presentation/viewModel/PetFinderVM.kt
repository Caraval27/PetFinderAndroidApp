package com.example.petfinderapp.presentation.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PetFinderVM() : ViewModel() {
    private val petFinderService : PetFinderService = PetFinderService()

    var searchImages = mutableStateOf<List<String>>(emptyList())
        private set

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _filteredPosts = MutableStateFlow<List<Post>>(emptyList())
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    val posts: StateFlow<List<Post>> = petFinderService.posts
    private var _postType = PostType.Looking

    init {
        viewModelScope.launch {
            posts.collect { allPosts ->
                applyFilters(allPosts)
            }
        }
    }

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

    fun loadCategories(context: Context) {
        _categories.value = petFinderService.loadCategories(context)
    }

    fun updateCategory(updatedCategory: Category) {
        _categories.value = _categories.value.map { category ->
            if (category.name == updatedCategory.name) updatedCategory else category
        }

        applyFilters(posts.value)
    }

    private fun applyFilters(allPosts: List<Post>) {
        val selectedSubcategories = _categories.value.flatMap { category ->
            category.subcategories.filter { it.isSelected }.map { it.name }
        }

        _filteredPosts.value = if (selectedSubcategories.isEmpty()) {
            allPosts
        } else {
            allPosts.filter { post ->
                selectedSubcategories.contains(post.animalType)
            }
        }
    }

    fun updateSearchImages(newImages: List<String>) {
        searchImages.value = newImages
    }
}