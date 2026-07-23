package com.tgm.tgmc.feature.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.core.data.local.TgmcDataStore
import com.tgm.tgmc.ui.theme.*
import kotlinx.coroutines.delay
import javax.inject.Inject

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToParent: () -> Unit,
    onNavigateToChild: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val role by viewModel.userRole.collectAsStateWithLifecycle(initialValue = null)

    // Logo animation
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }
    val pulseScale = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        // Pop-in entrance
        scale.animateTo(1.1f, animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ))
        scale.animateTo(1f, animationSpec = tween(200))
        alpha.animateTo(1f, animationSpec = tween(400))

        // Breathing pulse
        repeat(2) {
            pulseScale.animateTo(1.05f, animationSpec = tween(600))
            pulseScale.animateTo(1f, animationSpec = tween(600))
        }

        delay(300)
    }

    // Navigate when role is determined
    LaunchedEffect(role) {
        if (role != null) {
            delay(1800)
            when (role) {
                UserRole.PARENT -> onNavigateToParent()
                UserRole.CHILD  -> onNavigateToChild()
                else            -> onNavigateToAuth()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(Navy700, Navy900),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value * pulseScale.value)
                    .alpha(alpha.value)
                    .background(
                        Brush.radialGradient(listOf(Cyan400, Indigo400)),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TGM",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = Navy900,
                        fontWeight = FontWeight.Black,
                        fontSize = 28.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "TGM-C",
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                ),
                modifier = Modifier.alpha(alpha.value)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Guardian Control",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Cyan400
                ),
                modifier = Modifier.alpha(alpha.value)
            )
        }
    }
}
