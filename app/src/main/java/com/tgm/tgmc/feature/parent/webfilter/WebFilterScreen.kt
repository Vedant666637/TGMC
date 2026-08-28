package com.tgm.tgmc.feature.parent.webfilter

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebFilterScreen(
    onBack: () -> Unit,
    viewModel: WebFilterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Domains", "Keywords")

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
                                "Web Filter",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = ClayTextTitle,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                            Text(
                                "Block domains & explicit terms",
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
                    actions = {
                        Box(
                            modifier = Modifier
                                .padding(end = 16.dp)
                                .size(40.dp)
                                .clay(
                                    backgroundColor = ClayPrimary,
                                    cornerRadius = 20.dp,
                                    elevation = 8.dp,
                                    lightShadowColor = ClayPrimary.copy(alpha = 0.6f),
                                    darkShadowColor = ClayShadowDark
                                )
                                .clip(CircleShape)
                                .clickable { showAddDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Rule", tint = ClayWhite)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            floatingActionButton = {
                Box(
                    modifier = Modifier
                        .clay(
                            backgroundColor = ClaySecondary,
                            cornerRadius = 28.dp,
                            elevation = 12.dp,
                            lightShadowColor = ClaySecondary.copy(alpha = 0.6f),
                            darkShadowColor = ClayShadowDark
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { viewModel.seedDefaults() }
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ClayWhite)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Apply Safety Defaults", fontWeight = FontWeight.Bold, color = ClayWhite)
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Custom Clay Tab Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(48.dp)
                        .insetClay(
                            backgroundColor = ClayCard,
                            cornerRadius = 24.dp,
                            elevation = 4.dp
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .padding(4.dp)
                                .then(
                                    if (isSelected) {
                                        Modifier.clay(
                                            backgroundColor = ClayBackground,
                                            cornerRadius = 20.dp,
                                            elevation = 4.dp
                                        )
                                    } else Modifier
                                )
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedTab = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                title,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) ClayPrimary else ClayTextBody
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = ClayPrimary, strokeWidth = 3.dp)
                    }
                } else if (uiState.error != null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(uiState.error!!, color = ClayAccent, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .clay(backgroundColor = ClayPrimary, cornerRadius = 16.dp, elevation = 6.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { viewModel.loadRules() }
                                    .padding(horizontal = 24.dp, vertical = 12.dp)
                            ) {
                                Text("Retry", color = ClayWhite, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    val filteredRules = when (selectedTab) {
                        0 -> uiState.rules.filter { it.ruleType == "DOMAIN" }
                        1 -> uiState.rules.filter { it.ruleType == "KEYWORD" }
                        else -> uiState.rules
                    }

                    if (filteredRules.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    if (selectedTab == 0) Icons.Default.Language else Icons.Default.TextFields,
                                    contentDescription = null,
                                    tint = ClayTextBody,
                                    modifier = Modifier.size(56.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    "No ${tabs[selectedTab].lowercase()} blocked yet.",
                                    color = ClayTextTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    "Tap + to add or use Safety Defaults.",
                                    color = ClayTextBody,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredRules, key = { it.id }) { rule ->
                                WebFilterRuleItem(
                                    rule = rule,
                                    onDelete = { viewModel.deleteRule(rule.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Rule Dialog
    if (showAddDialog) {
        AddWebFilterDialog(
            currentTab = selectedTab,
            onDismiss = { showAddDialog = false },
            onAdd = { ruleType, value ->
                viewModel.addRule(ruleType, value)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WebFilterRuleItem(
    rule: WebFilterRuleUi,
    onDelete: () -> Unit
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
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .insetClay(
                        backgroundColor = ClayBackground,
                        cornerRadius = 16.dp,
                        elevation = 4.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (rule.ruleType == "DOMAIN") Icons.Default.Language else Icons.Default.TextFields,
                    contentDescription = null,
                    tint = if (rule.ruleType == "DOMAIN") ClayAccent else Color(0xFFF59E0B),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.value,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ClayTextTitle,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
                Text(
                    text = rule.ruleType.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall.copy(color = ClayTextBody, fontWeight = FontWeight.Medium)
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clay(
                        backgroundColor = ClayCard,
                        cornerRadius = 20.dp,
                        elevation = 4.dp
                    )
                    .clip(CircleShape)
                    .clickable { onDelete() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ClayAccent, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWebFilterDialog(
    currentTab: Int,
    onDismiss: () -> Unit,
    onAdd: (ruleType: String, value: String) -> Unit
) {
    var inputValue by remember { mutableStateOf("") }
    val ruleType = if (currentTab == 0) "DOMAIN" else "KEYWORD"

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClayCard,
        title = {
            Text(
                "Block ${if (ruleType == "DOMAIN") "Domain" else "Keyword"}",
                color = ClayTextTitle,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column {
                Text(
                    if (ruleType == "DOMAIN")
                        "Enter a website domain to block (e.g. facebook.com). Subdomains will also be blocked."
                    else
                        "Enter a keyword to block. Any URL containing this word will be restricted.",
                    style = MaterialTheme.typography.bodySmall.copy(color = ClayTextBody)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .insetClay(backgroundColor = ClayBackground, cornerRadius = 16.dp, elevation = 4.dp)
                ) {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it.lowercase().trim() },
                        placeholder = {
                            Text(
                                if (ruleType == "DOMAIN") "facebook.com" else "gambling",
                                color = ClayTextBody
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = ClayTextTitle,
                            unfocusedTextColor = ClayTextTitle
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clay(
                        backgroundColor = ClayAccent,
                        cornerRadius = 14.dp,
                        elevation = 6.dp
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(enabled = inputValue.isNotBlank()) { onAdd(ruleType, inputValue) }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Block", fontWeight = FontWeight.Bold, color = ClayWhite)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ClayTextBody, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}
