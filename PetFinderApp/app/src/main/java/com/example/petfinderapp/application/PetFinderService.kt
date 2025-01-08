package com.example.petfinderapp.application

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.example.petfinderapp.domain.Category
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.domain.SubSubcategory
import com.example.petfinderapp.domain.Subcategory
import com.example.petfinderapp.infrastructure.RealtimeDbRepository
import com.example.petfinderapp.infrastructure.StorageRepository
import com.example.petfinderapp.infrastructure.TensorFlowLiteHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime
import kotlin.math.absoluteValue

class PetFinderService(
    val applicationContext: Context
){
    private val realtimeDbRepository : RealtimeDbRepository = RealtimeDbRepository()
    private val storageRepository : StorageRepository = StorageRepository()

    val posts: StateFlow<List<Post>> = realtimeDbRepository.posts
    val post: StateFlow<Post> = realtimeDbRepository.post
    val insertSucceeded: StateFlow<Boolean?> = realtimeDbRepository.insertSucceeded

    suspend fun createPost(post : Post) {
        val downloadUris : MutableList<String> = mutableListOf()
        for (index in post.images.indices) {
            val downloadUri = storageRepository.uploadImage(post.images[index])
            if (downloadUri != null) {
                downloadUris.add(downloadUri)
            }
        }
        post.images = downloadUris
        realtimeDbRepository.insertPost(post)
    }

    fun startStreamingPostFeed(postType: PostType) {
        realtimeDbRepository.addPostFeedListener(postType)
    }

    fun stopStreamingPostFeed(postType: PostType) {
        realtimeDbRepository.removePostFeedListener(postType)
    }

    fun startStreamingPostDetails(postId: String) {
        realtimeDbRepository.addPostDetailsListener(postId)
    }

    fun hasInternetConnection() : Boolean {
        val connectivityManager = applicationContext.getSystemService(ConnectivityManager::class.java)
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        return networkCapabilities != null && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
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

    fun loadCategories(context: Context): List<Category> {
        val categories = mutableListOf<Category>()
        val files = context.assets.list("") ?: emptyArray()

        for (fileName in files) {
            if (fileName.startsWith("Category") && fileName.endsWith(".txt")) {
                val lines = context.assets.open(fileName).bufferedReader().use { it.readLines() }
                if (lines.isNotEmpty()) {
                    val categoryName = lines[0]
                    val subcategories = lines.drop(1).map { subcategoryName ->
                        if (subcategoryName == "Dog" || subcategoryName == "Cat") {
                            val breedsFileName = when (subcategoryName) {
                                "Dog" -> "SubcategoryDogBreeds.txt"
                                "Cat" -> "SubcategoryCatBreeds.txt"
                                else -> null
                            }

                            val breeds = breedsFileName?.let { breedFile ->
                                context.assets.open(breedFile).bufferedReader().useLines { lines ->
                                    lines.mapIndexedNotNull { index, line ->
                                        if (subcategoryName == "Dog" && index == 10) null else SubSubcategory(name = line)
                                    }.toList()
                                }
                            } ?: emptyList()

                            Subcategory(name = subcategoryName, isSelected = false, subcategories = breeds)
                        } else {
                            Subcategory(name = subcategoryName)
                        }
                    }
                    categories.add(Category(name = categoryName, subcategories = subcategories))
                }
            }
        }

        return categories
    }

    fun applyFilterAndSortPosts(
        allPosts: List<Post>,
        categories : MutableStateFlow<List<Category>>
    ): List<Post> {
        val selectedFilters = categories.value.associate { category ->
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

        val filteredPosts = allPosts.filter { post ->
            val matchesAnimal = selectedAnimals.isEmpty() || selectedAnimals.keys.contains(post.animalType)

            val matchesBreed = selectedBreedsByAnimal[post.animalType]?.let { requiredBreeds ->
                requiredBreeds.isEmpty() || requiredBreeds.any { it in post.breed }
            } ?: true

            val matchesColor = selectedColors.isEmpty() || selectedColors.any { it in post.color }

            matchesAnimal && matchesBreed && matchesColor
        }

        return filteredPosts.sortedWith(compareBy(
            { post ->
                val selectedBreeds = selectedBreedsByAnimal[post.animalType] ?: emptySet()
                -post.breed.count { it in selectedBreeds }
            },
            { post ->
                val selectedBreeds = selectedBreedsByAnimal[post.animalType] ?: emptySet()
                if (selectedBreeds.isNotEmpty()) {
                    post.breed.size - post.breed.count { it in selectedBreeds }
                }
                else {
                    0
                }
            },
            { post ->
                -post.color.count { it in selectedColors }
            },
            { post ->
                if (selectedColors.isNotEmpty()) {
                    post.color.size - post.color.count { it in selectedColors }
                }
                else {
                    0
                }
            }
        ))
    }

    fun searchImage(
        context: Context,
        imageUri: Uri,
        filteredPosts: MutableStateFlow<List<Post>>
    ) {
        try {
            val bitmap = uriToBitmap(context, imageUri)

            if(bitmap != null) {
                val animalTypeLabels = loadAnimalTypes(context)
                val animalTypeModel = TensorFlowLiteHelper(context, "trained_model_cat_and_dog.tflite")
                val allPosts = posts.value

                val (animalTypeLabel, animalTypeConfidence) = extractAnimalType(bitmap, animalTypeModel, animalTypeLabels)
                val predictionResult = Pair(animalTypeLabel, animalTypeConfidence)
                println(predictionResult)

                if (animalTypeLabel == "Dog") {
                    val dogBreedLabels = loadAnimalBreeds(context, "Dog")
                    val dogBreedModel = TensorFlowLiteHelper(context, "trained_model_dog_photos_40_epochs.tflite")
                    val matchingDogBreeds = extractDogBreed(bitmap, dogBreedModel, dogBreedLabels)

                    if (matchingDogBreeds.isEmpty()) {
                        filteredPosts.value = allPosts.filter { post ->
                            post.animalType == animalTypeLabel
                        }
                    } else {
                        filteredPosts.value = allPosts
                            .filter { post ->
                                post.breed.any { it in matchingDogBreeds.map { it.first } }
                            }
                            .sortedBy { post ->
                                post.breed.size - matchingDogBreeds.filter { it.first in post.breed }.sumOf { it.second.toDouble() }
                            }
                    }
                } else if (animalTypeLabel == "Cat") {
                    val catBreedLabels = loadAnimalBreeds(context, "Cat")
                    val catBreedModel = TensorFlowLiteHelper(context, "trained_model_cat_photos_40_epochs.tflite")
                    val matchingCatBreeds = extractCatBreed(bitmap, catBreedModel, catBreedLabels)

                    if (matchingCatBreeds.isEmpty()) {
                        filteredPosts.value = allPosts.filter { post ->
                            post.animalType == animalTypeLabel
                        }
                    } else {
                        filteredPosts.value = allPosts
                            .filter { post ->
                                post.breed.any { it in matchingCatBreeds.map { it.first } }
                            }
                            .sortedBy { post ->
                                post.breed.size - matchingCatBreeds.filter { it.first in post.breed }.sumOf { it.second.toDouble() }
                            }

                    }
                } else {
                    Toast.makeText(context, "No matches on image search", Toast.LENGTH_SHORT).show()
                }

                if (filteredPosts.value.isEmpty()) {
                    Toast.makeText(context, "No matches on image search", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Failed to load picture", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
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
            if (confidence >= 0.1) {
                val label = labels.getOrNull(index) ?: "Unknown"
                println("Dog Breed: $label, Confidence: $confidence")
                dogBreedLabelsWithConfidence.add(label to confidence)
            }
        }
        return dogBreedLabelsWithConfidence
    }

    private fun extractCatBreed(bitmap: Bitmap, model: TensorFlowLiteHelper, labels: List<String>): List<Pair<String, Float>> {
        val inputBuffer = model.preprocessImage(bitmap)
        val result = model.runModel(inputBuffer, outputSize = 38)
        val catBreedLabelsWithConfidence = mutableListOf<Pair<String, Float>>()

        for (index in result.indices) {
            val confidence = result[index]
            if (confidence >= 0.1) {
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