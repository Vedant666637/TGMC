package com.tgm.tgmc.feature.parent.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
                                "Activity Reports",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Screen time & app metrics",
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clay(
                                backgroundColor = ClayCard,
                                cornerRadius = 28.dp,
                                elevation = 12.dp,
                                lightShadowColor = ClayShadowLight,
                                darkShadowColor = ClayShadowDark
                            )
                            .padding(24.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .insetClay(backgroundColor = ClayBackground, cornerRadius = 20.dp, elevation = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = ClaySecondary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Total Screen Time Today",
                                    style = MaterialTheme.typography.titleMedium.copy(color = ClayTextBody, fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "4h 45m",
                                style = MaterialTheme.typography.displayMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.Black)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { 0.75f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .insetClay(backgroundColor = ClayBackground, cornerRadius = 5.dp, elevation = 2.dp),
                                color = ClayPrimary,
                                trackColor = Color.Transparent,
                                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "75% of daily limit (6h total)",
                                style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                item {
                    Text(
                        "App Usage Breakdown",
                        style = MaterialTheme.typography.titleMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.ExtraBold),
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp, start = 4.dp)
                    )
                }

                items(appUsage) { usage ->
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
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                usage.first,
                                style = MaterialTheme.typography.titleMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.Bold)
                            )
                            Box(
                                modifier = Modifier
                                    .insetClay(backgroundColor = ClayBackground, cornerRadius = 14.dp, elevation = 4.dp)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    usage.second,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = ClayPrimary, fontWeight = FontWeight.ExtraBold)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
