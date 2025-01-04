package com.example.petfinderapp.presentation.viewModel

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.utils.TensorFlowLiteHelper

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime


class PetFinderVM(
    application: Application
) : AndroidViewModel(application) {
    private val petFinderService : PetFinderService = PetFinderService(application.applicationContext)

    var searchImages = mutableStateOf<List<String>>(emptyList())
        private set

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _filteredPosts = MutableStateFlow<List<Post>>(emptyList())
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    private val posts: StateFlow<List<Post>> = petFinderService.posts

    val post: StateFlow<Post> = petFinderService.post

    private var _postType = PostType.Looking

    private val _hasInternetConnection = MutableStateFlow(true)
    val hasInternetConnection: StateFlow<Boolean> = _hasInternetConnection

    var predictionResult = mutableStateOf<Pair<String, Float>?>(null)

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
        if (postId != post.value.id) {
            viewModelScope.launch {
                petFinderService.startStreamingPostDetails(postId)
            }
        }
    }

    fun setHasInternetConnection(hasInternetConnection: Boolean) {
        _hasInternetConnection.value = hasInternetConnection
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
            "Dog" -> "SubcategoryDogBreeds.txt"
            "Cat" -> "SubcategoryCatBreeds.txt"
            else -> return emptyList()
        }
        return context.assets.open(fileName)
            .bufferedReader()
            .useLines { lines -> lines.toList() }
    }

    fun loadColors(context: Context): List<String> {
        return context.assets.open("CategoryColor.txt")
            .bufferedReader()
            .useLines { lines ->
                lines.drop(1).toList()
            }
    }

    fun loadLabels(context: Context): List<String> {
        return context.assets.open("Labels.txt")
            .bufferedReader()
            .useLines { lines -> lines.toList() }
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
            category.name to category.subcategories.filter { it.isSelected }
                .associate { subcategory ->
                    subcategory.name to subcategory.subcategories.filter { it.isSelected }
                        .map { it.name }
                }
        }

        val selectedAnimals = selectedFilters["Animal"].orEmpty()
        val selectedColors = selectedFilters["Color"]
            ?.flatMap { it.value.ifEmpty { listOf(it.key) } }
            ?: emptyList()
        val selectedBreedsByAnimal = selectedAnimals.mapValues { (_, breeds) -> breeds.toSet() }

        _filteredPosts.value = allPosts.filter { post ->
            val matchesAnimal = selectedAnimals.isEmpty() || selectedAnimals.keys.contains(post.animalType)

            val matchesBreed = selectedBreedsByAnimal[post.animalType]?.let { requiredBreeds ->
                requiredBreeds.isEmpty() || requiredBreeds.all { it in post.breed }
            } ?: true

            val matchesColor = selectedColors.isEmpty() || selectedColors.all { it in post.color }

            matchesAnimal && matchesBreed && matchesColor
        }
    }

    fun updateSearchImages(newImages: List<String>) {
        searchImages.value = newImages
    }

    fun searchImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = uriToBitmap(context, imageUri)
                val labels = loadLabels(context)

                if(bitmap != null) {
                    val tensorFlowHelper = TensorFlowLiteHelper(context)

                    // två modeller kolla först katt/hund
                    // kolla sen färg
                    // sist filter
                    val inputBuffer = tensorFlowHelper.preprocessImage(bitmap)
                    val result = tensorFlowHelper.runModel(inputBuffer, outputSize = 33)
                    val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1

                    println("maxindex : " + maxIndex)

                    val confidence = if (maxIndex != -1) result[maxIndex] else 0f
                    val label = if (maxIndex != -1) labels[maxIndex] else "Unknown"

                    predictionResult.value = Pair(label, confidence)
                    println(predictionResult.value)

                    val allPosts = posts.value
                    _filteredPosts.value = allPosts.filter { post ->
                        post.breed.all { it in label }
                    }
                } else {
                    Toast.makeText(context, "Failed to convert to bitmap", Toast.LENGTH_SHORT).show()
                }
            } catch (e:Exception){
                e.printStackTrace()
            }

        }
    }

    private fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}