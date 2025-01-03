package com.example.petfinderapp.presentation.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.components.FeedGrid
import com.example.petfinderapp.presentation.components.ImageCard
import com.example.petfinderapp.presentation.components.PostDetails
import com.example.petfinderapp.presentation.components.SearchByPictureButton
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun FoundScreen(
    petFinderVM: PetFinderVM
) {
    val context = LocalContext.current
    var selectedPost by remember { mutableStateOf<Post?>(null)}

    LaunchedEffect(Unit) {
        petFinderVM.initFeed(PostType.Found)
    }
    if (selectedPost == null) {
        FeedGrid(petFinderVM = petFinderVM, onImageClick = { post -> selectedPost = post })
    }
    else {
        PostDetails(selectedPost!!)
    }
}