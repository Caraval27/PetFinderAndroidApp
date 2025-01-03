package com.example.petfinderapp.presentation.viewModel

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petfinderapp.application.PetFinderService
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.utils.TensorFlowLiteHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class PetFinderVM : ViewModel() {
    private val petFinderService : PetFinderService = PetFinderService()
    var searchImages = mutableStateOf<List<String>>(emptyList())
        private set
    val posts: StateFlow<List<Post>> = petFinderService.posts
    private var _postType = PostType.Looking
    var predictionResult = mutableStateOf<Pair<String, Float>?>(null)

    val labels = listOf(
        "cat", "dog", "horse", "elephant", "bird", "fish", //lägg med fler
    )

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

    fun updateSearchImages(newImages: List<String>) {
        searchImages.value = newImages
    }


    fun searchImage(context: Context,imageUri: Uri) {
        viewModelScope.launch {
            try {
                val bitmap = uriToBitmap(context, imageUri)

                if(bitmap != null) {
                    // Preprocess the image
                    val inputBuffer = TensorFlowLiteHelper(context,).preprocessImage(bitmap)
                    val result = TensorFlowLiteHelper(context).runModel(inputBuffer, outputSize = 1001)

                    val maxIndex = result.indices.maxByOrNull { result[it] } ?: -1
                    val confidence = if (maxIndex != -1) result[maxIndex] else 0f
                    val label = if (maxIndex != -1) labels[maxIndex] else "Unknown"

                    predictionResult.value = Pair(label, confidence)
                    val matchingPosts = petFinderService.searchPostsByAnimalType(label)

                    //val (label, confidence) = result
                    //predictionResult = result
                } else {
                    Toast.makeText(context, "Failed to convert to bitmap", Toast.LENGTH_SHORT).show()
                }

                // Handle the result (output from the model)

            } catch (e:Exception){
                e.printStackTrace()
            }

        }
    }


    fun uriToBitmap(context: Context, uri: Uri): Bitmap? {
        return try {
            val contentResolver: ContentResolver = context.contentResolver
            // Open the input stream for the URI
            val inputStream = contentResolver.openInputStream(uri)
            // Decode the input stream into a Bitmap
            BitmapFactory.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}