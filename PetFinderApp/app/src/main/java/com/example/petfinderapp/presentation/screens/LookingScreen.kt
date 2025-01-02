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
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.components.ImageCard
import com.example.petfinderapp.presentation.components.SearchByPictureButton
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun LookingScreen(
    petFinderVM: PetFinderVM
) {
    val context = LocalContext.current
    val posts = petFinderVM.posts.collectAsState()

    LaunchedEffect(Unit) {
        petFinderVM.initFeed(PostType.Looking)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        SearchByPictureButton(
            context = context,
            petFinderVM = petFinderVM
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(posts.value) { post ->
                ImageCard(imageUri = post.images[0])
            }
        }
    }
}