package com.tgm.tgmc.feature.parent.audio

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveAudioScreen(
    onBack: () -> Unit,
    viewModel: LiveAudioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopListening()
        }
    }

    // Audio sound wave animation pulse
    val infiniteTransition = rememberInfiniteTransition(label = "audio_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (uiState.isStreaming) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

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
                                "Live Audio Stream",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Ambient microphone listening",
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
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                // Pulse graphic container with Claymorphism
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(220.dp)
                ) {
                    if (uiState.isStreaming) {
                        // Outer pulse ring
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(pulseScale)
                                .clay(
                                    backgroundColor = ClayBackground,
                                    cornerRadius = 110.dp,
                                    elevation = 4.dp,
                                    lightShadowColor = ClayShadowLight,
                                    darkShadowColor = ClayShadowDark
                                )
                        )
                        // Inner ring
                        Box(
                            modifier = Modifier
                                .size(160.dp)
                                .insetClay(
                                    backgroundColor = ClayBackground,
                                    cornerRadius = 80.dp,
                                    elevation = 6.dp
                                )
                        )
                    }

                    // Central mic orb
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clay(
                                backgroundColor = if (uiState.isStreaming) Color(0xFFF59E0B) else ClayCard,
                                cornerRadius = 55.dp,
                                elevation = 14.dp,
                                lightShadowColor = ClayShadowLight,
                                darkShadowColor = ClayShadowDark
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (uiState.isStreaming) Icons.Default.VolumeUp else Icons.Default.Mic,
                            contentDescription = "Mic Status",
                            tint = if (uiState.isStreaming) ClayWhite else ClayTextBody,
                            modifier = Modifier.size(44.dp)
                        )
                    }
                }

                // Status card
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
                        .padding(24.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (uiState.isStreaming) "Listening Live" else "Mic Stream Idle",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = ClayTextTitle,
                                fontWeight = FontWeight.ExtraBold
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (uiState.isStreaming) "PCM audio is streaming to your speaker in real-time."
                            else "Connect to the child's microphone to monitor ambient audio. A compliance notification is shown on the child device.",
                            style = MaterialTheme.typography.bodySmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Controls block
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (uiState.error != null) {
                        Text(
                            uiState.error!!,
                            color = ClayAccent,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }

                    if (!uiState.isStreaming) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clay(
                                    backgroundColor = Color(0xFFF59E0B),
                                    cornerRadius = 20.dp,
                                    elevation = 8.dp
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.startListening() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = ClayWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Live Listening", fontWeight = FontWeight.Bold, color = ClayWhite)
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
                                .clickable { viewModel.stopListening() }
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Stop, contentDescription = null, tint = ClayWhite)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Stop Audio Stream", fontWeight = FontWeight.Bold, color = ClayWhite)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
