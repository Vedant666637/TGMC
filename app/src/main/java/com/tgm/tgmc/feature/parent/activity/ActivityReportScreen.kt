package com.tgm.tgmc.feature.parent.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityReportScreen(
    onBack: () -> Unit
) {
    // Mock data for Phase 2 MVP
    val appUsage = remember {
        listOf(
            Pair("YouTube", "1h 45m"),
            Pair("TikTok", "1h 20m"),
            Pair("Chrome", "45m"),
            Pair("Minecraft", "30m"),
            Pair("Instagram", "25m")
        )
    }

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.BarChart, contentDescription = null, tint = Cyan400, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Activity Report", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Surface(
                    color = Surface800,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = Cyan400)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Total Screen Time Today", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("4h 45m", style = MaterialTheme.typography.displayMedium.copy(color = TextPrimary, fontWeight = FontWeight.Black))
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { 0.75f },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                            color = Cyan400,
                            trackColor = Surface600,
                            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("75% of daily limit (6h)", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                    }
                }
            }

            item {
                Text("App Usage", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary), modifier = Modifier.padding(vertical = 8.dp))
            }

            items(appUsage) { usage ->
                Surface(
                    color = Surface800,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(usage.first, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.SemiBold))
                        Text(usage.second, style = MaterialTheme.typography.bodyMedium.copy(color = Cyan400, fontWeight = FontWeight.Bold))
                    }
                }
            }
        }
    }
}
