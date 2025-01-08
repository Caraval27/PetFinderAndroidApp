package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun FeedGrid(
    petFinderVM: PetFinderVM,
    navController: NavHostController
) {
    val context = LocalContext.current
    val filteredPosts by petFinderVM.filteredPosts.collectAsState()
    val gridState = rememberLazyGridState()

    LaunchedEffect(Unit) {
        if (!petFinderVM.isReturningFromDetails) {
            petFinderVM.loadFilterCategories(context)
        } else {
            petFinderVM.updateIsReturningFromDetails(false)
        }
    }

    LaunchedEffect(filteredPosts) {
        gridState.scrollToItem(0)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        TopBar(petFinderVM = petFinderVM)

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            state = gridState
        ) {
            items(filteredPosts) { post ->
                ImageCard(post = post, navController = navController)
            }
        }
    }
}