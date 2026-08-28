package com.tgm.tgmc.feature.parent.pairing

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingStartScreen(onGenerateQr: () -> Unit, onBack: () -> Unit) {
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
                                "Add Child Device",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Device pairing wizard",
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
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Hero icon
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clay(
                            backgroundColor = ClayCard,
                            cornerRadius = 36.dp,
                            elevation = 14.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.PhonelinkSetup, contentDescription = null, tint = ClayPrimary, modifier = Modifier.size(56.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Pair a Child Device",
                    style = MaterialTheme.typography.titleLarge.copy(color = ClayTextTitle, fontWeight = FontWeight.ExtraBold)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Generate a pairing code, then have the child install TGM-C and enter it.",
                    style = MaterialTheme.typography.bodySmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(28.dp))

                // Steps card
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
                        .padding(18.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        listOf(
                            Triple(Icons.Default.Download, "Step 1", "Child installs TGM-C app"),
                            Triple(Icons.Default.QrCode, "Step 2", "Tap 'Generate Code' below"),
                            Triple(Icons.Default.QrCodeScanner, "Step 3", "Child enters the pairing code"),
                            Triple(Icons.Default.CheckCircle, "Step 4", "Devices are linked instantly")
                        ).forEachIndexed { i, (icon, step, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .insetClay(backgroundColor = ClayBackground, cornerRadius = 14.dp, elevation = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = if (i == 3) Color(0xFF10B981) else ClayPrimary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(step, style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody))
                                    Text(desc, style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clay(
                            backgroundColor = ClayPrimary,
                            cornerRadius = 20.dp,
                            elevation = 10.dp
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { onGenerateQr() }
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCode2, contentDescription = null, tint = ClayWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate Pairing Code", fontWeight = FontWeight.Bold, color = ClayWhite)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingQrScreen(
    onBack: () -> Unit,
    viewModel: PairingViewModel = hiltViewModel()
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
                                "Pairing Code",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Share with child device",
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // QR placeholder card
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clay(
                            backgroundColor = ClayWhite,
                            cornerRadius = 28.dp,
                            elevation = 14.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = ClayTextTitle, modifier = Modifier.size(150.dp))
                }

                Spacer(modifier = Modifier.height(28.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator(color = ClayPrimary, strokeWidth = 3.dp)
                } else if (uiState.error != null) {
                    Text(
                        text = uiState.error ?: "An error occurred",
                        color = ClayAccent,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .clay(backgroundColor = ClayPrimary, cornerRadius = 14.dp, elevation = 6.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.generateCode() }
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text("Retry", color = ClayWhite, fontWeight = FontWeight.Bold)
                    }
                } else {
                    // Code display with inset clay
                    Box(
                        modifier = Modifier
                            .insetClay(backgroundColor = ClayBackground, cornerRadius = 20.dp, elevation = 6.dp)
                            .padding(horizontal = 28.dp, vertical = 14.dp)
                    ) {
                        Text(
                            text = uiState.code ?: "XXXX-XXXX",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                color = ClayPrimary,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 4.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "This code expires in 30 days",
                    style = MaterialTheme.typography.bodySmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Instruction card
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
                    Text(
                        "⚙️  Enter this 8-digit code on the Child's device to complete pairing.",
                        style = MaterialTheme.typography.labelMedium.copy(color = ClayTextBody, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}
