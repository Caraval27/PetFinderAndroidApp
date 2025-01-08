package com.example.petfinderapp.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.petfinderapp.presentation.components.BottomNavBar
import com.example.petfinderapp.presentation.screens.CreatePostScreen
import com.example.petfinderapp.presentation.screens.FoundScreen
import com.example.petfinderapp.presentation.screens.SearchingScreen
import com.example.petfinderapp.presentation.screens.PostDetailsScreen
import com.example.petfinderapp.presentation.theme.PetFinderAppTheme
import com.example.petfinderapp.presentation.utils.Screen
import com.example.petfinderapp.presentation.viewModel.PetFinderVM

class MainActivity : ComponentActivity() {
    private lateinit var petFinderVM: PetFinderVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        petFinderVM = ViewModelProvider(this)[PetFinderVM::class.java]
        enableEdgeToEdge()

        setContent {
            PetFinderAppTheme {
                val navController : NavHostController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val hasInternetConnection = petFinderVM.hasInternetConnection.collectAsState()
                val insertSucceeded = petFinderVM.insertSucceeded.collectAsState()

                LaunchedEffect(hasInternetConnection.value) {
                    if (!hasInternetConnection.value) {
                        snackbarHostState.showSnackbar("No internet connection")
                        petFinderVM.setHasInternetConnection(true)
                    }
                }

                LaunchedEffect(insertSucceeded.value) {
                    if (insertSucceeded.value == false) {
                        snackbarHostState.showSnackbar("Error creating post")
                        petFinderVM.setInsertSucceeded(null)
                    }
                }

                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        composable(Screen.Searching.route) {
                            SearchingScreen(petFinderVM = petFinderVM, navController = navController)
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