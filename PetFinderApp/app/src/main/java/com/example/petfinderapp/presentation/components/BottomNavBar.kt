package com.example.petfinderapp.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.petfinderapp.presentation.utils.Screen

@Composable
fun BottomNavBar(navController: NavController) {
    val items = listOf(
        Screen.Found,
        Screen.Searching,
        Screen.CreatePost
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    when (val icon = screen.icon) {
                        is Int -> Icon(
                            painter = painterResource(id = icon),
                            contentDescription = screen.label,
                            modifier = Modifier.size(24.dp)
                        )
                        is ImageVector -> Icon(
                            imageVector = icon,
                            contentDescription = screen.label
                        )
                        else -> throw IllegalArgumentException("Unsupported icon type")
                    }
                },
                label = { Text(screen.label) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                    }
                }
            )
        }
    }
}