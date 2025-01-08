package com.example.petfinderapp.presentation.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import com.example.petfinderapp.R

sealed class Screen(val route: String, val icon: Any, val label: String) {
    data object Found : Screen("found", R.drawable.found_icon, "Found")
    data object Searching : Screen("searching", Icons.Filled.Search, "Searching")
    data object CreatePost : Screen("post", Icons.Default.Add, "Post")
}