package com.example.petfinderapp.presentation.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    data object Found : Screen("found", Icons.Default.Home, "Found")
    data object Looking : Screen("looking", Icons.Default.Person, "Looking")
    data object CreatePost : Screen("create", Icons.Default.Add, "Create")
}