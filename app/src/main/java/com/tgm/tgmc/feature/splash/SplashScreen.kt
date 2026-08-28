package com.tgm.tgmc.feature.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.ui.theme.ClayBackground
import com.tgm.tgmc.ui.theme.ClayPrimary

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToParent: () -> Unit,
    onNavigateToChild: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val role by viewModel.userRole.collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(role) {
        if (role != null) {
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
            .background(ClayBackground),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = ClayPrimary)
    }
}
