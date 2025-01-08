package com.example.petfinderapp.presentation.viewModel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class PetFinderVM(
    application: Application
) : AndroidViewModel(application) {
    private val petFinderService : PetFinderService = PetFinderService(application.applicationContext)

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _filteredPosts = MutableStateFlow<List<Post>>(emptyList())
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    private val posts: StateFlow<List<Post>> = petFinderService.posts

    val post: StateFlow<Post> = petFinderService.post

    private val _insertSucceeded = MutableStateFlow<Boolean?>(null)
    val insertSucceeded : StateFlow<Boolean?> = _insertSucceeded

    private var _postType = PostType.Searching

    private val _hasInternetConnection = MutableStateFlow(true)
    val hasInternetConnection: StateFlow<Boolean> = _hasInternetConnection

    var isReturningFromDetails: Boolean = false

    init {
        viewModelScope.launch {
            posts.collect { allPosts ->
                applyFilters(allPosts)
            }
        }
        viewModelScope.launch {
            petFinderService.insertSucceeded.collect {
                _insertSucceeded.value = it
            }
        }
    }

    fun createPost(
        title: String,
        animalType: String,
        breed: List<String>,
        color: List<String>,
        userName: String,
        phoneNumber: String,
        description: String,
        postType: PostType,
        images: List<String>
    ) {
        val post =
            Post(
                title = title,
                time = LocalDateTime.now().toString(),
                animalType = animalType,
                breed = breed,
                color = color,
                userName = userName,
                phoneNumber = phoneNumber,
                description = description,
                postType = postType,
                images = images
            )
        viewModelScope.launch {
            _hasInternetConnection.value = petFinderService.hasInternetConnection()
            petFinderService.createPost(post)
        }
    }

    fun initFeed(postType: PostType) {
        viewModelScope.launch {
            _hasInternetConnection.value = petFinderService.hasInternetConnection()
            if (postType != _postType) {
                petFinderService.stopStreamingPostFeed(_postType)
                _postType = postType
                petFinderService.startStreamingPostFeed(postType)
            }
        }
    }

    fun initDetails(postId: String) {
        viewModelScope.launch {
            _hasInternetConnection.value = petFinderService.hasInternetConnection()
            if (postId != post.value.id) {
                petFinderService.startStreamingPostDetails(postId)
            }
        }
    }

    fun setHasInternetConnection(hasInternetConnection: Boolean) {
        _hasInternetConnection.value = hasInternetConnection
    }

    fun setInsertSucceeded(insertSucceeded: Boolean?) {
        _insertSucceeded.value = insertSucceeded
    }

    fun updateIsReturningFromDetails(update: Boolean) {
        isReturningFromDetails = update
    }

    fun loadAnimalTypes(context: Context): List<String> {
        return petFinderService.loadAnimalTypes(context)
    }

    fun loadAnimalBreeds(context: Context, animalType: String): List<String> {
        return petFinderService.loadAnimalBreeds(context, animalType)
    }

    fun loadColors(context: Context): List<String> {
        return petFinderService.loadColors(context)
    }

    fun loadFilterCategories(context: Context) {
        _categories.value = petFinderService.loadCategories(context)
        applyFilters(posts.value)
    }

    fun updateFilterCategory(updatedCategory: Category) {
        _categories.value = _categories.value.map { category ->
            if (category.name == updatedCategory.name) updatedCategory else category
        }

        applyFilters(posts.value)
    }

    private fun applyFilters(allPosts: List<Post>) {
        viewModelScope.launch {
            _filteredPosts.value = petFinderService.applyFilterAndSortPosts(
                allPosts = allPosts,
                categories = _categories
            )
        }
    }

    fun searchImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            petFinderService.searchImage(context, imageUri, _filteredPosts)
        }
    }
}