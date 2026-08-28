package com.tgm.tgmc.feature.parent.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val isLoggedOut by viewModel.isLoggedOut.collectAsStateWithLifecycle()
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(isLoggedOut) {
        if (isLoggedOut) {
            onLogout()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
                .size(200.dp)
                .clay(
                    backgroundColor = ClayBackground,
                    cornerRadius = 100.dp,
                    elevation = 20.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Settings",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "App configuration & account",
                                style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody)
                            )
                        }
                    },
                    navigationIcon = {
                        Box(
                            modifier = Modifier
                                .padding(start = 12.dp, end = 8.dp)
                                .size(40.dp)
                                .clay(
                                    backgroundColor = ClayCard,
                                    cornerRadius = 20.dp,
                                    elevation = 6.dp,
                                    lightShadowColor = ClayShadowLight,
                                    darkShadowColor = ClayShadowDark
                                )
                                .clip(CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = ClayTextTitle)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingRow(Icons.Default.Person, "Account", "Manage email and security credentials")
                    SettingRow(Icons.Default.FamilyRestroom, "Family Sharing", "Invite co-parents & guardians")
                    SettingRow(Icons.Default.ChildCare, "Child Devices", "Manage paired devices & profiles")
                    SettingRow(Icons.Default.Notifications, "Notifications", "Real-time alert preferences")
                    SettingRow(Icons.Default.Security, "Privacy & Control", "Data retention & cloud settings")
                    SettingRow(Icons.Default.HelpOutline, "Help & Support", "Knowledge base and support")
                    SettingRow(Icons.Default.Info, "About TGM-C", "Version 1.0.0 (Build 2026)")
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Logout Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clay(
                            backgroundColor = ClayCard,
                            cornerRadius = 20.dp,
                            elevation = 8.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showLogoutDialog = true }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Logout, contentDescription = null, tint = ClayAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Sign Out", style = MaterialTheme.typography.titleMedium.copy(color = ClayAccent, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .insetClay(backgroundColor = ClayBackground, cornerRadius = 28.dp, elevation = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, tint = ClayAccent, modifier = Modifier.size(28.dp))
                }
            },
            title = {
                Text(
                    "Sign Out",
                    style = MaterialTheme.typography.titleLarge.copy(color = ClayTextTitle, fontWeight = FontWeight.ExtraBold)
                )
            },
            text = {
                Text(
                    "Are you sure you want to sign out of your account?",
                    style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody)
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clay(backgroundColor = ClayAccent, cornerRadius = 14.dp, elevation = 6.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            showLogoutDialog = false
                            viewModel.logout()
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text("Sign Out", color = ClayWhite, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = ClayTextBody, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = ClayCard
        )
    }
}

@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clay(
                backgroundColor = ClayCard,
                cornerRadius = 20.dp,
                elevation = 8.dp,
                lightShadowColor = ClayShadowLight,
                darkShadowColor = ClayShadowDark
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .insetClay(
                        backgroundColor = ClayBackground,
                        cornerRadius = 14.dp,
                        elevation = 4.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium)
                )
            }

            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = ClayTextBody)
        }
    }
}
