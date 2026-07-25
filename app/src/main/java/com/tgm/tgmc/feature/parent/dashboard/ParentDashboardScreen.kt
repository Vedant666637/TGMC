package com.tgm.tgmc.feature.parent.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.core.domain.model.ChildDevice
import com.tgm.tgmc.navigation.TgmcRoutes
import com.tgm.tgmc.ui.theme.*

data class DashboardFeature(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val accentColor: Color,
    val isActive: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentDashboardScreen(
    onNavigateTo: (String) -> Unit,
    onLogout: () -> Unit,
    viewModel: ParentDashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) {
            onLogout()
        }
    }

    val features = remember {
        listOf(
            DashboardFeature("App Block",     "Manage blocked apps",      Icons.Default.Block,       TgmcRoutes.Parent.APP_BLOCK, ClayAccent),
            DashboardFeature("Web Filter",    "Block domains & keywords", Icons.Default.FilterAlt,   TgmcRoutes.Parent.WEB_FILTER, ClayPrimary),
            DashboardFeature("Activity",      "App usage & reports",      Icons.Default.BarChart,    TgmcRoutes.Parent.ACTIVITY_REPORT, ClaySecondary),
            DashboardFeature("Schedule",      "Screen time rules",        Icons.Default.Schedule,    TgmcRoutes.Parent.SCHEDULE,  Color(0xFFF59E0B)),
            DashboardFeature("Location",      "GPS & geofences",          Icons.Default.LocationOn,  TgmcRoutes.Parent.LOCATION,  Color(0xFF10B981)),
            DashboardFeature("Camera",        "Remote camera access",     Icons.Default.CameraAlt,   TgmcRoutes.Parent.CAMERA,    ClaySecondary),
            DashboardFeature("Screen Mirror", "Live screen view",         Icons.Default.ScreenShare, TgmcRoutes.Parent.MIRROR,    ClayPrimary),
            DashboardFeature("Live Audio",    "Live microphone stream",   Icons.Default.Mic,         TgmcRoutes.Parent.AUDIO,     Color(0xFFF59E0B)),
            DashboardFeature("Alerts",        "Notifications & events",   Icons.Default.Notifications, TgmcRoutes.Parent.ALERTS,  ClaySecondary),
            DashboardFeature("Pair Device",   "Add a child device",       Icons.Default.AddCircle,   TgmcRoutes.Parent.PAIRING_START, Color(0xFF10B981)),
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground)
    ) {
        // Decorative 3D Orbs (Claymorphism hallmark)
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-40).dp)
                .size(250.dp)
                .clay(
                    backgroundColor = ClayBackground,
                    cornerRadius = 125.dp,
                    elevation = 30.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 100.dp, y = 150.dp)
                .size(300.dp)
                .clay(
                    backgroundColor = ClayBackground,
                    cornerRadius = 150.dp,
                    elevation = 40.dp,
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
                                "TGM-C",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayPrimary, fontWeight = FontWeight.Black, letterSpacing = 1.sp
                                )
                            )
                            Text(
                                "Guardian Dashboard",
                                style = MaterialTheme.typography.labelMedium.copy(color = ClayTextBody, fontWeight = FontWeight.SemiBold)
                            )
                        }
                    },
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(48.dp)
                                .clay(
                                    backgroundColor = ClayCard,
                                    cornerRadius = 24.dp,
                                    elevation = 8.dp,
                                    lightShadowColor = ClayShadowLight,
                                    darkShadowColor = ClayShadowDark
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { onNavigateTo(TgmcRoutes.Parent.ALERTS) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Alerts", tint = ClayAccent)
                        }
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(48.dp)
                                .clay(
                                    backgroundColor = ClayCard,
                                    cornerRadius = 24.dp,
                                    elevation = 8.dp,
                                    lightShadowColor = ClayShadowLight,
                                    darkShadowColor = ClayShadowDark
                                )
                                .clip(RoundedCornerShape(24.dp))
                                .clickable { onNavigateTo(TgmcRoutes.Parent.SETTINGS) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings", tint = ClayTextTitle)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { paddingValues ->
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 150.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Status card
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    ChildStatusCard(
                        selectedDevice = uiState.selectedDevice,
                        devices = uiState.devices,
                        onDeviceSelected = { viewModel.selectDevice(it) }
                    )
                }

                // Section Title
                item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                    Text(
                        "Parental Controls",
                        style = MaterialTheme.typography.titleMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp)
                    )
                }

                // Feature grid
                items(features) { feature ->
                    FeatureCard(
                        feature = feature,
                        onClick = { onNavigateTo(feature.route) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
private fun ChildStatusCard(
    selectedDevice: ChildDevice?,
    devices: List<ChildDevice>,
    onDeviceSelected: (ChildDevice) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clay(
                backgroundColor = ClayCard,
                cornerRadius = 32.dp,
                elevation = 16.dp,
                lightShadowColor = ClayShadowLight,
                darkShadowColor = ClayShadowDark
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { if (devices.size > 1) showDropdown = true }
                    ) {
                        Text(
                            "Monitoring Device",
                            style = MaterialTheme.typography.labelMedium.copy(color = ClayTextBody, fontWeight = FontWeight.SemiBold)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                selectedDevice?.model ?: "No device paired",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle, fontWeight = FontWeight.ExtraBold
                                )
                            )
                            if (devices.size > 1) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch device", tint = ClayPrimary)
                            }
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false },
                            modifier = Modifier.background(ClayBackground)
                        ) {
                            devices.forEach { device ->
                                DropdownMenuItem(
                                    text = { Text(device.model, color = ClayTextTitle, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        onDeviceSelected(device)
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clay(
                                backgroundColor = ClayBackground,
                                cornerRadius = 32.dp,
                                elevation = 8.dp,
                                lightShadowColor = ClayShadowLight,
                                darkShadowColor = ClayShadowDark
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val isOnline = selectedDevice?.isOnline == true
                    val statusText = if (selectedDevice != null) {
                        if (isOnline) "Online" else "Offline"
                    } else "Offline"
                    
                    val statusColor = if (isOnline) Color(0xFF10B981) else ClayAccent

                    StatusChip(statusText, statusColor)
                    StatusChip(
                        selectedDevice?.model ?: "Pair to begin",
                        ClayPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, indicatorColor: Color) {
    Box(
        modifier = Modifier
            .height(36.dp)
            .insetClay(
                backgroundColor = ClayCard,
                cornerRadius = 18.dp,
                elevation = 4.dp,
                lightShadowColor = ClayShadowLight,
                darkShadowColor = ClayShadowDark
            )
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(indicatorColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    feature: DashboardFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(140.dp)
            .clay(
                backgroundColor = ClayCard,
                cornerRadius = 24.dp,
                elevation = 12.dp,
                lightShadowColor = ClayShadowLight,
                darkShadowColor = ClayShadowDark
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .insetClay(
                        backgroundColor = ClayBackground, // Carved out background
                        cornerRadius = 16.dp,
                        elevation = 6.dp,
                        lightShadowColor = ClayShadowLight,
                        darkShadowColor = ClayShadowDark
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.accentColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ClayTextTitle, fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody, fontWeight = FontWeight.SemiBold),
                    maxLines = 1
                )
            }
        }
    }
}
