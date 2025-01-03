package com.example.petfinderapp.presentation.screens

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.petfinderapp.domain.Post
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.components.FeedGrid
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun LookingScreen(
    petFinderVM: PetFinderVM,
    navController: NavHostController
) {
    LaunchedEffect(Unit) {
        petFinderVM.initFeed(PostType.Looking)
    }

    FeedGrid(petFinderVM = petFinderVM, navController = navController)
}