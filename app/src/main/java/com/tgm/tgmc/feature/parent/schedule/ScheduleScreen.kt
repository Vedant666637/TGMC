package com.tgm.tgmc.feature.parent.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(onBack: () -> Unit) {
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
                                "Time Schedules",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Downtime & bedtime rules",
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clay(
                            backgroundColor = ClayCard,
                            cornerRadius = 50.dp,
                            elevation = 16.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Schedule,
                        contentDescription = null,
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    "Smart Screen Time Rules",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = ClayTextTitle,
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Set downtime windows, bedtime restrictions, and per-app daily usage caps for balanced device habits.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clay(
                            backgroundColor = ClayCard,
                            cornerRadius = 24.dp,
                            elevation = 8.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        )
                        .padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .insetClay(backgroundColor = ClayBackground, cornerRadius = 20.dp, elevation = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Bedtime, contentDescription = null, tint = ClayPrimary)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Bedtime Downtime active from 9:00 PM to 7:00 AM",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = ClayTextTitle,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }
        }
    }
}
