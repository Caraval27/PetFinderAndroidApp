package com.example.petfinderapp.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Found : Screen("found", Icons.Filled.Check, "Found")
    data object Looking : Screen("looking", Icons.Filled.Search, "Looking")
    data object CreatePost : Screen("post", Icons.Default.Add, "Post")
}