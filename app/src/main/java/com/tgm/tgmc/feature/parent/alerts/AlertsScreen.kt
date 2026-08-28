package com.tgm.tgmc.feature.parent.alerts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.core.domain.model.AlertItem
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    onBack: () -> Unit,
    viewModel: AlertsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
                                "Alert Center",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Safety notifications & requests",
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
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = ClayPrimary, strokeWidth = 3.dp)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = ClayAccent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .clay(backgroundColor = ClayPrimary, cornerRadius = 16.dp, elevation = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { viewModel.loadAlerts() }
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Retry", color = ClayWhite, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (uiState.alerts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clay(backgroundColor = ClayCard, cornerRadius = 45.dp, elevation = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = ClayTextBody, modifier = Modifier.size(48.dp))
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("No New Alerts", style = MaterialTheme.typography.titleLarge.copy(color = ClayTextTitle, fontWeight = FontWeight.ExtraBold))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("You're completely caught up.", style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(uiState.alerts, key = { it.id }) { alert ->
                        AlertItemCard(
                            alert = alert,
                            onRead = { viewModel.markAsRead(alert.id) },
                            onResolve = { action -> viewModel.resolvePurchaseRequest(alert.id, action) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertItemCard(
    alert: AlertItem,
    onRead: () -> Unit,
    onResolve: (String) -> Unit
) {
    val isPurchaseRequest = alert.type.name == "PURCHASE_REQUEST" && !alert.message.contains("Status:")
    val icon = when (alert.type.name) {
        "SOS" -> Icons.Default.Warning
        "APP_INSTALL" -> Icons.Default.GetApp
        "GEOFENCE" -> Icons.Default.LocationOn
        "PURCHASE_REQUEST" -> Icons.Default.ShoppingCart
        else -> Icons.Default.Notifications
    }
    val tint = when (alert.type.name) {
        "SOS" -> ClayAccent
        "GEOFENCE" -> Color(0xFF10B981)
        "PURCHASE_REQUEST" -> Color(0xFFF59E0B)
        else -> ClayPrimary
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clay(
                backgroundColor = ClayCard,
                cornerRadius = 24.dp,
                elevation = 10.dp,
                lightShadowColor = ClayShadowLight,
                darkShadowColor = ClayShadowDark
            )
            .clip(RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .insetClay(
                            backgroundColor = ClayBackground,
                            cornerRadius = 16.dp,
                            elevation = 4.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ClayTextTitle,
                            fontWeight = if (alert.isRead) FontWeight.Bold else FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodySmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium)
                    )
                }
                if (!alert.isRead && !isPurchaseRequest) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clay(backgroundColor = ClayCard, cornerRadius = 16.dp, elevation = 4.dp)
                            .clip(CircleShape)
                            .clickable { onRead() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Mark as read", tint = ClayPrimary, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (isPurchaseRequest) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clay(backgroundColor = ClayCard, cornerRadius = 16.dp, elevation = 4.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onResolve("DENIED") }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Decline", style = MaterialTheme.typography.labelMedium.copy(color = ClayAccent, fontWeight = FontWeight.Bold))
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clay(backgroundColor = ClayPrimary, cornerRadius = 16.dp, elevation = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onResolve("APPROVED") }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Approve", style = MaterialTheme.typography.labelMedium.copy(color = ClayWhite, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
