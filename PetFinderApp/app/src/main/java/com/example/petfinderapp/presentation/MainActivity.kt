package com.example.petfinderapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import com.example.petfinderapp.presentation.screens.CreatePostScreen
import com.example.petfinderapp.presentation.theme.PetFinderAppTheme
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

class MainActivity : ComponentActivity() {
    private lateinit var petFinderVM: PetFinderVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        petFinderVM = ViewModelProvider(this)[PetFinderVM::class.java]
        enableEdgeToEdge()

        setContent {
            PetFinderAppTheme {
                Surface (
                    modifier = Modifier.fillMaxSize()
                ) {
                    CreatePostScreen(petFinderVM)
                }
            }
        }
    }
}