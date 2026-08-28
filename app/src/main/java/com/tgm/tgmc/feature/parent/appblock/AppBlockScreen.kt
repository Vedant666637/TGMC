package com.tgm.tgmc.feature.parent.appblock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.core.domain.model.AppInfo
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBlockScreen(
    onBack: () -> Unit,
    viewModel: AppBlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }

    val filteredApps = remember(uiState.apps, searchQuery) {
        uiState.apps.filter {
            it.appName.contains(searchQuery, ignoreCase = true) ||
            it.packageName.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClayBackground)
    ) {
        // Decorative background orb
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
                                "App Control",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Block or allow app access",
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
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Inset Clay Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .insetClay(
                            backgroundColor = ClayCard,
                            cornerRadius = 20.dp,
                            elevation = 6.dp,
                            lightShadowColor = ClayShadowLight,
                            darkShadowColor = ClayShadowDark
                        )
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search installed apps...", color = ClayTextBody) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = ClayPrimary) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = ClayTextBody)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = ClayTextTitle,
                            unfocusedTextColor = ClayTextTitle
                        ),
                        shape = RoundedCornerShape(20.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClayPrimary, strokeWidth = 3.dp)
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = ClayAccent, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(uiState.error!!, color = ClayAccent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(16.dp))
                            Box(
                                modifier = Modifier
                                    .clay(
                                        backgroundColor = ClayPrimary,
                                        cornerRadius = 16.dp,
                                        elevation = 6.dp
                                    )
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.loadDeviceAndApps() }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text("Retry", color = ClayWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else if (filteredApps.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AppBlocking, contentDescription = null, tint = ClayTextBody, modifier = Modifier.size(56.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isEmpty()) "No apps found on this device." else "No matching apps found.",
                                color = ClayTextBody,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(filteredApps, key = { it.packageName }) { app ->
                            AppBlockItem(
                                app = app,
                                onToggleBlock = { viewModel.toggleAppBlock(app) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AppBlockItem(
    app: AppInfo,
    onToggleBlock: () -> Unit
) {
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
            .clip(RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon with Inset Clay Container
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .insetClay(
                        backgroundColor = ClayBackground,
                        cornerRadius = 18.dp,
                        elevation = 4.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (app.category) {
                        com.tgm.tgmc.core.domain.model.AppCategory.SOCIAL -> Icons.Default.People
                        com.tgm.tgmc.core.domain.model.AppCategory.GAMES -> Icons.Default.Gamepad
                        com.tgm.tgmc.core.domain.model.AppCategory.STREAMING -> Icons.Default.PlayCircle
                        com.tgm.tgmc.core.domain.model.AppCategory.EDUCATION -> Icons.Default.Book
                        com.tgm.tgmc.core.domain.model.AppCategory.PRODUCTIVITY -> Icons.Default.Build
                        com.tgm.tgmc.core.domain.model.AppCategory.COMMUNICATION -> Icons.Default.Chat
                        com.tgm.tgmc.core.domain.model.AppCategory.OTHER -> Icons.Default.Android
                    },
                    contentDescription = null,
                    tint = if (app.isBlocked) ClayAccent else ClayPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ClayTextTitle,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = app.isBlocked,
                onCheckedChange = { onToggleBlock() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = ClayWhite,
                    checkedTrackColor = ClayAccent,
                    uncheckedThumbColor = ClayTextBody,
                    uncheckedTrackColor = ClayBackground
                )
            )
        }
    }
}
