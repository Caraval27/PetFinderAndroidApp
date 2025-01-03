package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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
    val categories by petFinderVM.categories.collectAsState()
    val filteredPosts by petFinderVM.filteredPosts.collectAsState()

    LaunchedEffect(Unit) {
        petFinderVM.loadFilterCategories(context)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(0.8f)
                    .align(Alignment.Top)
            ) {
                FilterButton(
                    categories = categories,
                    onCategorySelectionChange = { updatedCategory ->
                        petFinderVM.updateFilterCategory(updatedCategory)
                    }
                )
            }

            Column(
                modifier = Modifier
                    .weight(0.2f)
                    .align(Alignment.Top)
            ) {
                SearchByPictureButton(
                    context = context,
                    petFinderVM = petFinderVM
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(filteredPosts) { post ->
                ImageCard(post = post, navController = navController)
            }
        }
    }
}