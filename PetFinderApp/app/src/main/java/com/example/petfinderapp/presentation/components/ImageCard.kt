package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberAsyncImagePainter

@Composable
fun ImageCard(imageUri: String, onImageClick: () -> Unit) {
    Image(
        painter = rememberAsyncImagePainter(model = imageUri),
        contentDescription = "Post image",
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clickable(onClick = onImageClick),
        contentScale = ContentScale.Crop
    )
}