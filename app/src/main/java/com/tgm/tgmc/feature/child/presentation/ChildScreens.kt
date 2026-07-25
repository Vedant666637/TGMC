package com.tgm.tgmc.feature.child.presentation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
import com.tgm.tgmc.ui.theme.*

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

// ── Child Pair Screen ─────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildPairScreen(
    onPaired: () -> Unit,
    viewModel: ChildPairViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onPaired()
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(ClayBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ChildCare, contentDescription = null, tint = ClaySecondary, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Enter Pairing Code", style = MaterialTheme.typography.headlineSmall.copy(color = ClayTextTitle, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ask your parent for the code shown on their TGM-C app.", style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier
                .fillMaxWidth()
                .clay(
                    backgroundColor = ClayWhite,
                    cornerRadius = 16.dp,
                    elevation = 6.dp,
                    lightShadowColor = ClayShadowLight,
                    darkShadowColor = ClayShadowDark
                )) {
                OutlinedTextField(
                    value = uiState.code,
                    onValueChange = { viewModel.onCodeChange(it.uppercase()) },
                    label = { Text("e.g. XXXX-XXXX", color = ClayTextBody) },
                    singleLine = true,
                    isError = uiState.error != null,
                    supportingText = uiState.error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = ClayPrimary,
                        unfocusedTextColor = ClayPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val context = LocalContext.current
            val scanner = remember { GmsBarcodeScanning.getClient(context) }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Scan QR Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clay(
                            backgroundColor = ClayCard,
                            cornerRadius = 16.dp,
                            elevation = 6.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        )
                        .clickable(enabled = !uiState.isLoading) {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    barcode.rawValue?.let { code ->
                                        viewModel.onCodeChange(code)
                                    }
                                }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR", tint = ClayPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Scan QR", fontWeight = FontWeight.Bold, color = ClayTextTitle)
                    }
                }

                // Pair Device Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clay(
                            backgroundColor = ClayPrimary,
                            cornerRadius = 16.dp,
                            elevation = 8.dp,
                            lightShadowColor = ClayPrimary.copy(alpha = 0.6f),
                            darkShadowColor = ClayShadowDark
                        )
                        .clickable(enabled = !uiState.isLoading, onClick = { viewModel.activateCode() }),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ClayWhite, strokeWidth = 2.dp)
                    else Text("Pair", fontWeight = FontWeight.Bold, color = ClayWhite)
                }
            }
        }
    }
}

// ── Child Consent Screen ─────────────────────────────────────────
@Composable
fun ChildConsentScreen(
    onAccepted: () -> Unit,
    viewModel: ChildPairViewModel = hiltViewModel()
) {
    val monitoringItems = listOf(
        "📍 Location is tracked and visible to your parent",
        "📱 App usage and installed apps are monitored",
        "⏰ Screen time limits may be enforced",
        "📸 Your parent may activate the camera",
        "🔊 Your parent may listen via microphone",
        "🖥️ Your parent may view your screen",
        "⚠️ A visible notification is always shown when monitoring is active"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(ClayBackground),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ClaySecondary, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Before You Continue", style = MaterialTheme.typography.headlineSmall.copy(color = ClayTextTitle, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text("This device is monitored by your parent using TGM-C. Here's what that means:", style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

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
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    monitoringItems.forEach { item ->
                        Text(item, style = MaterialTheme.typography.bodySmall.copy(color = ClayTextBody))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clay(
                        backgroundColor = ClayPrimary,
                        cornerRadius = 16.dp,
                        elevation = 8.dp,
                        lightShadowColor = ClayPrimary.copy(alpha = 0.6f),
                        darkShadowColor = ClayShadowDark
                    )
                    .clickable(onClick = {
                        viewModel.approveConsent()
                        onAccepted()
                    }),
                contentAlignment = Alignment.Center
            ) {
                Text("I Understand, Continue", fontWeight = FontWeight.Bold, color = ClayWhite)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ── Child Home Screen ─────────────────────────────────────────────
@Composable
fun ChildHomeScreen(
    viewModel: ChildPairViewModel = hiltViewModel()
) {
    // SOS button pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "sos_pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sos_scale"
    )

    Box(
        modifier = Modifier.fillMaxSize().background(ClayBackground)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Monitoring indicator
            Surface(
                color = ClaySecondary.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TGM-C parental monitoring is active", style = MaterialTheme.typography.labelSmall.copy(color = ClaySecondary))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val context = androidx.compose.ui.platform.LocalContext.current
            val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
            var isIgnoringBattery by remember {
                mutableStateOf(com.tgm.tgmc.core.util.BatteryOptimizationUtil.isIgnoringBatteryOptimizations(context))
            }

            DisposableEffect(lifecycleOwner) {
                val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
                    if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                        isIgnoringBattery = com.tgm.tgmc.core.util.BatteryOptimizationUtil.isIgnoringBatteryOptimizations(context)
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                }
            }

            if (!isIgnoringBattery) {
                Surface(
                    color = ClayAccent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            com.tgm.tgmc.core.util.BatteryOptimizationUtil.requestIgnoreBatteryOptimizations(context)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryAlert,
                            contentDescription = "Battery Alert",
                            tint = ClayAccent,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Background protection limited",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = ClayAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "Tap to exclude TGM-C from battery optimizations",
                                style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Hello 👋", style = MaterialTheme.typography.headlineMedium.copy(color = ClayTextTitle, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Your device is linked to your parent's account.", style = MaterialTheme.typography.bodyMedium.copy(color = ClayTextBody), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(32.dp))

            // Screen time card
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
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("Screen Time Today", style = MaterialTheme.typography.labelMedium.copy(color = ClayTextBody))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3h 24m", style = MaterialTheme.typography.headlineLarge.copy(color = ClayTextTitle, fontWeight = FontWeight.Black))
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.57f },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clay(
                            backgroundColor = ClayWhite,
                            cornerRadius = 4.dp,
                            elevation = 2.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        ),
                        color = ClayPrimary,
                        trackColor = Color.Transparent
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("6h daily limit · 2h 36m remaining", style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // SOS Button
            Text("Emergency", style = MaterialTheme.typography.labelMedium.copy(color = ClayTextBody))
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulse)
                    .background(ClayAccent.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clay(
                            backgroundColor = ClayAccent,
                            cornerRadius = 55.dp,
                            elevation = 12.dp,
                            lightShadowColor = ClayAccent.copy(alpha = 0.6f),
                            darkShadowColor = ClayShadowDark
                        )
                        .clickable(onClick = { viewModel.triggerSos() }),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Sos, contentDescription = "SOS", tint = ClayWhite, modifier = Modifier.size(36.dp))
                        Text("SOS", fontWeight = FontWeight.Black, fontSize = 12.sp, color = ClayWhite)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Hold to send emergency alert", style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody))
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// needed for Color reference in ChildHomeScreen
private val Color = androidx.compose.ui.graphics.Color
