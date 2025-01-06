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

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _filteredPosts = MutableStateFlow<List<Post>>(emptyList())
    val filteredPosts: StateFlow<List<Post>> = _filteredPosts

    private val posts: StateFlow<List<Post>> = petFinderService.posts

    val post: StateFlow<Post> = petFinderService.post

    private var _postType = PostType.Looking

    private val _hasInternetConnection = MutableStateFlow(true)
    val hasInternetConnection: StateFlow<Boolean> = _hasInternetConnection

    var isReturningFromDetails: Boolean = false

    private var predictionResult = mutableStateOf<Pair<String, Float>?>(null)

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

    fun updateIsReturningFromDetails(update: Boolean) {
        isReturningFromDetails = update
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
            "Cat" -> "SubcategoryCatBreeds.txt"
            "Dog" -> "SubcategoryDogBreeds.txt"
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

    fun searchImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = uriToBitmap(context, imageUri)

                if(bitmap != null) {
                    //Load labels
                    val animalTypeLabels = loadAnimalTypes(context)
                    val dogBreedLabels = loadAnimalBreeds(context, "Dog")
                    val catBreedLabels = loadAnimalBreeds(context, "Cat")

                    //Load different ML models
                    val animalTypeModel = TensorFlowLiteHelper(context, "trained_model_cat_and_dog.tflite")
                    val dogBreedModel = TensorFlowLiteHelper(context, "trained_model_dog_photos_40_epochs.tflite")
                    val catBreedModel = TensorFlowLiteHelper(context, "trained_model_cat_photos_40_epochs.tflite")

                    //Determine animal type (cat or dog)
                    val (animalTypeLabel, animalTypeConfidence) = extractAnimalType(bitmap, animalTypeModel, animalTypeLabels)
                    predictionResult.value = Pair(animalTypeLabel, animalTypeConfidence)
                    println(predictionResult.value)

                    val allPosts = posts.value

                    if (animalTypeLabel == "Dog") {
                        //Determine dog breeds
                        val matchingDogBreeds = extractDogBreed(bitmap, dogBreedModel, dogBreedLabels)

                        if (matchingDogBreeds.isEmpty()) {
                            //If there are no breeds with a match of at least 50% confidence
                            _filteredPosts.value = allPosts.filter { post ->
                                post.animalType == animalTypeLabel
                            }
                        } else {
                            _filteredPosts.value = allPosts.filter { post ->
                                post.breed.any { it in matchingDogBreeds.map { it.first } }
                            }
                        }
                    } else if (animalTypeLabel == "Cat") {
                        //Determine cat breeds
                        val matchingCatBreeds = extractCatBreed(bitmap, catBreedModel, catBreedLabels)


                        if (matchingCatBreeds.isEmpty()) {
                            //If there are no breeds with a match of at least 50% confidence
                            _filteredPosts.value = allPosts.filter { post ->
                                post.animalType == animalTypeLabel
                            }
                        } else {
                            _filteredPosts.value = allPosts.filter { post ->
                                post.breed.any { it in matchingCatBreeds.map { it.first } }
                            }
                        }
                    } else {
                        Toast.makeText(context, "No matches on image search", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Toast.makeText(context, "Failed to load picture", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun extractAnimalType(bitmap: Bitmap, model: TensorFlowLiteHelper, labels: List<String>): Pair<String, Float> {
        val inputBuffer = model.preprocessImage(bitmap)
        val result = model.runModel(inputBuffer, outputSize = 2)
        val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
        val confidence = if (maxIndex != -1) result[maxIndex] else 0f
        val label = if (maxIndex != -1) labels[maxIndex] else "Unknown"
        return Pair(label, confidence)
    }

    private fun extractDogBreed(bitmap: Bitmap, model: TensorFlowLiteHelper, labels: List<String>): List<Pair<String, Float>> {
        val inputBuffer = model.preprocessImage(bitmap)
        val result = model.runModel(inputBuffer, outputSize = 58)
        val dogBreedLabelsWithConfidence = mutableListOf<Pair<String, Float>>()

        for (index in result.indices) {
            val confidence = result[index]
            if (confidence >= 0.5) {
                val label = labels.getOrNull(index) ?: "Unknown"
                println("Dog Breed: $label, Confidence: $confidence")
                dogBreedLabelsWithConfidence.add(label to confidence)
            }
        }
        return dogBreedLabelsWithConfidence
    }

    fun extractCatBreed(bitmap: Bitmap, model: TensorFlowLiteHelper, labels: List<String>): List<Pair<String, Float>> {
        val inputBuffer = model.preprocessImage(bitmap)
        val result = model.runModel(inputBuffer, outputSize = 38)
        val catBreedLabelsWithConfidence = mutableListOf<Pair<String, Float>>()

        for (index in result.indices) {
            val confidence = result[index]
            if (confidence >= 0.5) {
                val label = labels.getOrNull(index) ?: "Unknown"
                println("Cat Breed: $label, Confidence: $confidence")
                catBreedLabelsWithConfidence.add(label to confidence)
            }
        }
        return catBreedLabelsWithConfidence
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