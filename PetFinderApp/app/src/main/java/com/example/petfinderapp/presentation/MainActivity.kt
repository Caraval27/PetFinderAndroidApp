package com.example.petfinderapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petfinderapp.presentation.components.BottomNavBar
import com.example.petfinderapp.presentation.screens.CreatePostScreen
import com.example.petfinderapp.presentation.screens.FoundScreen
import com.example.petfinderapp.presentation.screens.LookingScreen
import com.example.petfinderapp.presentation.screens.PostDetailsScreen
import com.example.petfinderapp.presentation.theme.PetFinderAppTheme
import com.example.petfinderapp.presentation.viewModel.PetFinderVM
import com.google.firebase.Firebase
import com.google.firebase.database.database

class MainActivity : ComponentActivity() {
    private lateinit var petFinderVM: PetFinderVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Firebase.database.setPersistenceEnabled(true)
        petFinderVM = ViewModelProvider(this)[PetFinderVM::class.java]
        enableEdgeToEdge()

        setContent {
            PetFinderAppTheme {
                val navController : NavHostController = rememberNavController()

                Scaffold(
                    bottomBar = { BottomNavBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Found.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Found.route) {
                            FoundScreen(petFinderVM = petFinderVM, navController = navController)
                        }
                        composable(Screen.Looking.route) {
                            LookingScreen(petFinderVM = petFinderVM, navController = navController)
                        }
                        composable(Screen.CreatePost.route) {
                            CreatePostScreen(
                                petFinderVM = petFinderVM,
                                navController = navController
                            )
                        }
                        composable("details/{postId}") { navBackStackEntry ->
                            if (navBackStackEntry.arguments != null) {
                                val postId = navBackStackEntry.arguments!!.getString("postId")
                                if (postId != null) {
                                    PostDetailsScreen(
                                        petFinderVM = petFinderVM,
                                        postId = postId
                                    )

                                }
                            }
                        }
                    }
                }
            }
        }
    }
}