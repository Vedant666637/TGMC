package com.tgm.tgmc.feature.parent.dashboard

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tgm.tgmc.navigation.TgmcRoutes
import com.tgm.tgmc.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.core.domain.model.ChildDevice

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
            DashboardFeature("App Block",     "Manage blocked apps",      Icons.Default.Block,       TgmcRoutes.Parent.APP_BLOCK, ErrorRed),
            DashboardFeature("Web Filter",    "Block domains & keywords", Icons.Default.FilterAlt,   TgmcRoutes.Parent.WEB_FILTER,Indigo400),
            DashboardFeature("Activity",      "App usage & reports",      Icons.Default.BarChart,    TgmcRoutes.Parent.ACTIVITY_REPORT, Cyan400),
            DashboardFeature("Schedule",      "Screen time rules",        Icons.Default.Schedule,    TgmcRoutes.Parent.SCHEDULE,  WarningAmber),
            DashboardFeature("Location",      "GPS & geofences",          Icons.Default.LocationOn,  TgmcRoutes.Parent.LOCATION,  SuccessGreen),
            DashboardFeature("Camera",        "Remote camera access",     Icons.Default.CameraAlt,   TgmcRoutes.Parent.CAMERA,    Cyan400),
            DashboardFeature("Screen Mirror", "Live screen view",         Icons.Default.ScreenShare, TgmcRoutes.Parent.MIRROR,    Indigo400),
            DashboardFeature("Live Audio",    "Live microphone stream",   Icons.Default.Mic,         TgmcRoutes.Parent.AUDIO,     WarningAmber),
            DashboardFeature("Alerts",        "Notifications & events",   Icons.Default.Notifications, TgmcRoutes.Parent.ALERTS,  Cyan400),
            DashboardFeature("Pair Device",   "Add a child device",       Icons.Default.AddCircle,   TgmcRoutes.Parent.PAIRING_START, SuccessGreen),
        )
    }

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "TGM-C",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = Cyan400, fontWeight = FontWeight.Black
                            )
                        )
                        Text(
                            "Guardian Control",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateTo(TgmcRoutes.Parent.ALERTS) }) {
                        Badge(containerColor = ErrorRed) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Alerts", tint = TextPrimary)
                        }
                    }
                    IconButton(onClick = { onNavigateTo(TgmcRoutes.Parent.SETTINGS) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status card
            item {
                ChildStatusCard(
                    selectedDevice = uiState.selectedDevice,
                    devices = uiState.devices,
                    onDeviceSelected = { viewModel.selectDevice(it) }
                )
            }

            // Feature grid
            item {
                Text(
                    "Controls",
                    style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    features.chunked(2).forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { feature ->
                                FeatureCard(
                                    feature = feature,
                                    onClick = { onNavigateTo(feature.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Pad if odd number
                            if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                        }
                    }
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

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Surface800,
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(listOf(Navy700, Surface800)),
                    RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
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
                            "Active Device",
                            style = MaterialTheme.typography.labelMedium.copy(color = TextMuted)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                selectedDevice?.model ?: "No device paired yet",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = TextPrimary, fontWeight = FontWeight.SemiBold
                                )
                            )
                            if (devices.size > 1) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Switch device", tint = TextSecondary)
                            }
                        }

                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            devices.forEach { device ->
                                DropdownMenuItem(
                                    text = { Text(device.model) },
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
                            .size(48.dp)
                            .background(Navy600, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = Cyan400)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    val statusText = if (selectedDevice != null) {
                        if (selectedDevice.isOnline) "Online" else "Offline"
                    } else "Offline"
                    
                    val statusColor = if (selectedDevice?.isOnline == true) SuccessGreen else TextMuted

                    StatusChip(statusText, Surface600, statusColor)
                    StatusChip(
                        selectedDevice?.model ?: "Pair a device to begin",
                        Surface700,
                        if (selectedDevice != null) TextSecondary else Cyan400
                    )
                }
            }
        }
    }
}


@Composable
private fun StatusChip(label: String, containerColor: Color, textColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall.copy(color = textColor)
        )
    }
}

@Composable
private fun FeatureCard(
    feature: DashboardFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = Surface800,
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        feature.accentColor.copy(alpha = 0.15f),
                        RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = TextPrimary, fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted),
                    maxLines = 1
                )
            }
        }
    }
}
