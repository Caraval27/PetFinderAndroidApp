package com.example.petfinderapp.presentation.screens

import androidx.compose.runtime.*
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.components.FeedGrid
import com.example.petfinderapp.presentation.components.PostDetails
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun FoundScreen(
    petFinderVM: PetFinderVM
) {
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