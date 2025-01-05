package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.petfinderapp.domain.Post

@Composable
fun ImageCard(
    post: Post,
    navController: NavHostController
) {
    Image(
        painter = rememberAsyncImagePainter(model = post.images[0]),
        contentDescription = "Photo from post",
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clickable(onClick = { navController.navigate("details/${post.id}") }),
        contentScale = ContentScale.Crop
    )
}