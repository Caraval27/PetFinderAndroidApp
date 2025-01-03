package com.example.petfinderapp.presentation.screens

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import com.example.petfinderapp.domain.PostType
import com.example.petfinderapp.presentation.components.FeedGrid
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun FoundScreen(
    petFinderVM: PetFinderVM,
    navController: NavHostController
) {
    LaunchedEffect(Unit) {
        petFinderVM.initFeed(PostType.Found)
    }

    FeedGrid(petFinderVM = petFinderVM, navController = navController)
}