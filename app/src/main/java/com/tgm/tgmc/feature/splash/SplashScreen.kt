package com.tgm.tgmc.feature.splash

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.core.domain.model.UserRole
import com.tgm.tgmc.R

@Composable
fun SplashScreen(
    onNavigateToAuth: () -> Unit,
    onNavigateToParent: () -> Unit,
    onNavigateToChild: () -> Unit,
    viewModel: SplashViewModel = hiltViewModel()
) {
    val role by viewModel.userRole.collectAsStateWithLifecycle(initialValue = null)
    var isVideoFinished by remember { mutableStateOf(false) }

    LaunchedEffect(role, isVideoFinished) {
        if (role != null && isVideoFinished) {
            when (role) {
                UserRole.PARENT -> onNavigateToParent()
                UserRole.CHILD  -> onNavigateToChild()
                else            -> onNavigateToAuth()
            }
        }
    }

    // Video Player
    AndroidView(
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.splash_video}"))
                setOnCompletionListener {
                    isVideoFinished = true
                }
                setOnErrorListener { _, _, _ ->
                    // Fallback if video fails to play
                    isVideoFinished = true
                    true
                }
                start()
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}
