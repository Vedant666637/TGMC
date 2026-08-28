package com.tgm.tgmc.feature.child.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tgm.tgmc.feature.parent.presentation.BottomNavItem
import com.tgm.tgmc.feature.parent.presentation.PlaceholderScreen
import com.tgm.tgmc.navigation.TgmcRoutes
import com.tgm.tgmc.ui.theme.*

@Composable
fun ChildMainLayout(
    onSendAlert: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("Learn", TgmcRoutes.Child.FEED, Icons.Default.MenuBook),
        BottomNavItem("Store", TgmcRoutes.Child.STORE, Icons.Default.Storefront),
        BottomNavItem("Messages", TgmcRoutes.Child.MESSAGES, Icons.Default.Message)
    )

    Scaffold(
        containerColor = ClayBackground,
        bottomBar = {
            NavigationBar(
                containerColor = ClayCard,
                contentColor = ClaySecondary,
                tonalElevation = 0.dp
            ) {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title, fontWeight = FontWeight.SemiBold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ClayWhite,
                            selectedTextColor = ClaySecondary,
                            indicatorColor = ClaySecondary,
                            unselectedIconColor = ClayTextBody,
                            unselectedTextColor = ClayTextBody
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // SOS Alert Button
            FloatingActionButton(
                onClick = onSendAlert,
                containerColor = ClayAccent,
                contentColor = ClayWhite,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Emergency, contentDescription = "Emergency SOS")
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = TgmcRoutes.Child.FEED,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(TgmcRoutes.Child.FEED) {
                PlaceholderScreen("Learning Feed (Videos & Posts)")
            }
            composable(TgmcRoutes.Child.STORE) {
                // Completely white screen as requested
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White)
                )
            }
            composable(TgmcRoutes.Child.MESSAGES) {
                PlaceholderScreen("Approved Messaging")
            }
        }
    }
}
