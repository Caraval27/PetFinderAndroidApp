package com.example.petfinderapp.presentation.viewModel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.domain.Subcategory
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

    private val posts: StateFlow<List<Post>> = petFinderService.posts
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

    fun loadAnimalTypes(context: Context): List<String> {
        return context.assets.open("CategoryAnimalType.txt")
            .bufferedReader()
            .useLines { lines ->
                lines.drop(1).toList()
            }
    }

    fun loadAnimalBreeds(context: Context, animalType: String): List<String> {
        val fileName = when (animalType) {
            "Dog" -> "CategoryDogBreeds.txt"
            "Cat" -> "CategoryCatBreeds.txt"
            else -> return emptyList()
        }
        return context.assets.open(fileName)
            .bufferedReader()
            .useLines { lines -> lines.toList() }
    }

    fun loadDogBreeds(context: Context): List<Subcategory> {
        return context.assets.open("CategoryDogBreeds.txt")
            .bufferedReader()
            .useLines { lines ->
                lines.map { Subcategory(name = it, isSelected = false) }.toList()
            }
    }

    fun loadColors(context: Context): List<String> {
        return context.assets.open("CategoryColor.txt")
            .bufferedReader()
            .useLines { lines ->
                lines.drop(1).toList()
            }
    }

    fun loadFilterCategories(context: Context) {
        _categories.value = petFinderService.loadCategories(context)
    }

    fun updateFilterCategory(updatedCategory: Category) {
        _categories.value = _categories.value.map { category ->
            if (category.name == updatedCategory.name) updatedCategory else category
        }

        applyFilters(posts.value)
    }

    private fun applyFilters(allPosts: List<Post>) {
        val selectedFilters = _categories.value.associate { category ->
            category.name to category.subcategories.filter { it.isSelected }.map { it.name }
        }

        val selectedAnimals = selectedFilters["Animal"].orEmpty()
        val selectedColors = selectedFilters["Color"].orEmpty()

        _filteredPosts.value = if (selectedAnimals.isEmpty() && selectedColors.isEmpty()) {
            allPosts
        } else {
            allPosts.filter { post ->
                val matchesAnimal = selectedAnimals.isEmpty() || selectedAnimals.contains(post.animalType)
                val matchesColor = selectedColors.isEmpty() || selectedColors.contains(post.color)
                matchesAnimal && matchesColor
            }
        }
    }

    fun updateSearchImages(newImages: List<String>) {
        searchImages.value = newImages
    }
}