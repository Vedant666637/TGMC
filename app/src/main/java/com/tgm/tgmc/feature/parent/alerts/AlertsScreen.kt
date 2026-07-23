package com.tgm.tgmc.feature.parent.alerts

import androidx.compose.foundation.background
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

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = { Text("Alerts", color = TextPrimary, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Cyan400)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(uiState.error!!, color = ErrorRed)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = { viewModel.loadAlerts() }, colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900)) {
                        Text("Retry")
                    }
                }
            }
        } else if (uiState.alerts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No Alerts", style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("You're all caught up.", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
        "SOS" -> ErrorRed
        "GEOFENCE" -> SuccessGreen
        "PURCHASE_REQUEST" -> WarningAmber
        else -> Cyan400
    }

    Surface(
        color = Surface800,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(40.dp).background(tint.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall.copy(color = if (alert.isRead) TextSecondary else TextPrimary, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.message,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                    )
                }
                if (!alert.isRead && !isPurchaseRequest) {
                    IconButton(onClick = onRead, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Check, contentDescription = "Mark as read", tint = Cyan400, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (isPurchaseRequest) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onResolve("DENIED") },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text("Decline", style = MaterialTheme.typography.labelSmall)
                    }
                    Button(
                        onClick = { onResolve("APPROVED") },
                        modifier = Modifier.weight(1f).height(36.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Navy900)
                    ) {
                        Text("Approve", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
