package com.example.petfinderapp.presentation.screens

import androidx.compose.runtime.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.presentation.components.SearchByPictureButton
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

@Composable
fun FoundScreen(
    petFinderVM: PetFinderVM
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(rememberScrollState())
    ) {
        SearchByPictureButton(
            context = context,
            petFinderVM = petFinderVM
        )

        Spacer(modifier = Modifier.height(16.dp))

        //visar dem temporärt, ska bort sen
        LazyRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(petFinderVM.searchImages.value) { uri ->
                Box(
                    modifier = Modifier.size(150.dp),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                    )
                    IconButton(
                        onClick = {
                            petFinderVM.searchImages.value = petFinderVM.searchImages.value.filter { it != uri }
                        },
                        modifier = Modifier
                            .size(26.dp)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}