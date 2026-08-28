package com.tgm.tgmc.feature.parent.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        containerColor = ClayBackground,
        bottomBar = {
            NavigationBar(
                containerColor = ClayCard,
                contentColor = ClayPrimary,
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
                            selectedTextColor = ClayPrimary,
                            indicatorColor = ClayPrimary,
                            unselectedIconColor = ClayTextBody,
                            unselectedTextColor = ClayTextBody
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            // SOS / Alert Button with Clay elevation
            FloatingActionButton(
                onClick = onSendAlert,
                containerColor = ClayAccent,
                contentColor = ClayWhite,
                shape = CircleShape
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .clay(
                    backgroundColor = ClayCard,
                    cornerRadius = 24.dp,
                    elevation = 10.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )
                .padding(horizontal = 32.dp, vertical = 20.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ClayTextTitle,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
