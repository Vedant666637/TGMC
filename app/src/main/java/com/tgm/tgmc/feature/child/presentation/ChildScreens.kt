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
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy800, Navy900))),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.ChildCare, contentDescription = null, tint = Cyan400, modifier = Modifier.size(80.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text("Enter Pairing Code", style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Ask your parent for the code shown on their TGM-C app.", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.code,
                onValueChange = { viewModel.onCodeChange(it.uppercase()) },
                label = { Text("e.g. XXXX-XXXX") },
                singleLine = true,
                isError = uiState.error != null,
                supportingText = uiState.error?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.TextFieldDefaults.colors(),
                shape = RoundedCornerShape(12.dp),
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = Cyan400, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.activateCode() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !uiState.isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900)
            ) {
                if (uiState.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Navy900, strokeWidth = 2.dp)
                else Text("Pair Device", fontWeight = FontWeight.Bold)
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
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy800, Navy900))),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Cyan400, modifier = Modifier.size(64.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Before You Continue", style = MaterialTheme.typography.headlineSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Text("This device is monitored by your parent using TGM-C. Here's what that means:", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))

            Surface(color = Surface800, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    monitoringItems.forEach { item ->
                        Text(item, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    viewModel.approveConsent()
                    onAccepted()
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900)
            ) {
                Text("I Understand, Continue", fontWeight = FontWeight.Bold)
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
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Navy800, Navy900)))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Monitoring indicator
            Surface(
                color = Cyan400.copy(alpha = 0.12f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).background(SuccessGreen, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("TGM-C parental monitoring is active", style = MaterialTheme.typography.labelSmall.copy(color = Cyan400))
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
                    color = WarningAmber.copy(alpha = 0.15f),
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
                            tint = WarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                "Background protection limited",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    color = WarningAmber,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                "Tap to exclude TGM-C from battery optimizations",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Hello 👋", style = MaterialTheme.typography.headlineMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(4.dp))
            Text("Your device is linked to your parent's account.", style = MaterialTheme.typography.bodyMedium.copy(color = TextMuted), textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(32.dp))

            // Screen time card
            Surface(color = Surface800, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Screen Time Today", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("3h 24m", style = MaterialTheme.typography.headlineLarge.copy(color = TextPrimary, fontWeight = FontWeight.Black))
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { 0.57f },
                        modifier = Modifier.fillMaxWidth(),
                        color = Cyan400,
                        trackColor = Surface700
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("6h daily limit · 2h 36m remaining", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // SOS Button
            Text("Emergency", style = MaterialTheme.typography.labelMedium.copy(color = TextMuted))
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(pulse)
                    .background(SosRed.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = { viewModel.triggerSos() },
                    modifier = Modifier.size(110.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = SosRed, contentColor = Color(0xFFFFFFFF))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Sos, contentDescription = "SOS", modifier = Modifier.size(36.dp))
                        Text("SOS", fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Hold to send emergency alert", style = MaterialTheme.typography.labelSmall.copy(color = TextMuted))
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// needed for Color reference in ChildHomeScreen
private val Color = androidx.compose.ui.graphics.Color
