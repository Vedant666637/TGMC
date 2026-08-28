package com.tgm.tgmc.feature.parent.mirror

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenMirrorScreen(
    onBack: () -> Unit,
    viewModel: ScreenMirrorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopMirroring()
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
                                "Live Screen View",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Real-time display mirroring",
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Live Mirror Container
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clay(
                            backgroundColor = ClayCard,
                            cornerRadius = 28.dp,
                            elevation = 14.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        )
                        .clip(RoundedCornerShape(28.dp))
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.latestFrame != null) {
                            Image(
                                bitmap = uiState.latestFrame!!.asImageBitmap(),
                                contentDescription = "Live Mirrored Screen",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )

                            // Streaming status overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(16.dp)
                                    .clay(
                                        backgroundColor = Color(0xFF10B981),
                                        cornerRadius = 12.dp,
                                        elevation = 6.dp
                                    )
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "MIRRORING ACTIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ClayWhite,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(28.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .insetClay(backgroundColor = ClayBackground, cornerRadius = 45.dp, elevation = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isStreaming) Icons.Default.Sync else Icons.Default.ScreenShare,
                                        contentDescription = null,
                                        tint = ClaySecondary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    if (uiState.isStreaming) "Connecting screen feed..." else "Screen Mirror Idle",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = ClayTextTitle,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    if (uiState.isStreaming) "Requesting live screen feed from child device."
                                    else "Tap start below to mirror the child device's screen in real-time.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = ClayTextBody),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.error != null) {
                    Text(
                        uiState.error!!,
                        color = ClayAccent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (!uiState.isStreaming) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clay(
                                backgroundColor = ClaySecondary,
                                cornerRadius = 20.dp,
                                elevation = 8.dp
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.startMirroring() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ClayWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Live Screen Mirroring", fontWeight = FontWeight.Bold, color = ClayWhite)
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clay(
                                backgroundColor = ClayAccent,
                                cornerRadius = 20.dp,
                                elevation = 10.dp
                            )
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { viewModel.stopMirroring() }
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = ClayWhite)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Stop Screen Mirroring", fontWeight = FontWeight.Bold, color = ClayWhite)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
