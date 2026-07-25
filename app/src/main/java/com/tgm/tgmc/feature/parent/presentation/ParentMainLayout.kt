package com.tgm.tgmc.feature.parent.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tgm.tgmc.feature.parent.dashboard.ParentDashboardScreen
import com.tgm.tgmc.navigation.TgmcRoutes
import com.tgm.tgmc.ui.theme.*

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
)

@Composable
fun ParentMainLayout(
    onNavigateToGlobal: (String) -> Unit,
    onLogout: () -> Unit,
    onSendAlert: () -> Unit
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("Feed", TgmcRoutes.Parent.FEED, Icons.Default.DynamicFeed),
        BottomNavItem("Store", TgmcRoutes.Parent.STORE, Icons.Default.Storefront),
        BottomNavItem("Messages", TgmcRoutes.Parent.MESSAGES, Icons.Default.Message),
        BottomNavItem("Childs", TgmcRoutes.Parent.DASHBOARD, Icons.Default.FamilyRestroom)
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Navy800,
                contentColor = Cyan400
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
                        label = { Text(item.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = Cyan400,
                            indicatorColor = Cyan400,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // Shared SOS / Alert Button
            FloatingActionButton(
                onClick = onSendAlert,
                containerColor = ErrorRed,
                contentColor = TextPrimary
            ) {
                Icon(Icons.Default.Warning, contentDescription = "Send Alert")
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = TgmcRoutes.Parent.FEED,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(TgmcRoutes.Parent.FEED) {
                PlaceholderScreen("Feed & Educational Videos")
            }
            composable(TgmcRoutes.Parent.STORE) {
                // Completely white screen as requested
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.White)
                )
            }
            composable(TgmcRoutes.Parent.MESSAGES) {
                PlaceholderScreen("Message Logs (Intercepted Chats)")
            }
            composable(TgmcRoutes.Parent.DASHBOARD) {
                // The existing Dashboard screen with child controls
                ParentDashboardScreen(
                    onNavigateTo = onNavigateToGlobal,
                    onLogout = onLogout
                )
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = TextPrimary)
    }
}
