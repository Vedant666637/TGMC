package com.tgm.tgmc.feature.parent.webfilter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tgm.tgmc.ui.theme.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

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

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Cyan400, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Web Filter", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Rule", tint = Cyan400)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.seedDefaults() },
                containerColor = Cyan400,
                contentColor = Navy900
            ) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Apply Safety Defaults", fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Navy800,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Cyan400,
                        height = 3.dp
                    )
                },
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) Cyan400 else TextMuted
                            )
                        }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Cyan400)
                }
            } else if (uiState.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(uiState.error!!, color = ErrorRed, style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadRules() }) { Text("Retry") }
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
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No ${tabs[selectedTab].lowercase()} blocked yet.",
                                color = TextMuted,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "Tap + to add or use Safety Defaults.",
                                color = TextMuted,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
    Surface(
        color = Surface800,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = if (rule.ruleType == "DOMAIN") ErrorRed.copy(alpha = 0.15f) else WarningAmber.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (rule.ruleType == "DOMAIN") Icons.Default.Language else Icons.Default.TextFields,
                        contentDescription = null,
                        tint = if (rule.ruleType == "DOMAIN") ErrorRed else WarningAmber,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.value,
                    style = MaterialTheme.typography.titleSmall.copy(
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = rule.ruleType.lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = ErrorRed)
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
        containerColor = Surface800,
        title = {
            Text(
                "Block ${if (ruleType == "DOMAIN") "Domain" else "Keyword"}",
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text(
                    if (ruleType == "DOMAIN")
                        "Enter a website domain to block (e.g. facebook.com). Subdomains will also be blocked."
                    else
                        "Enter a keyword to block. Any URL containing this word will be restricted.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inputValue,
                    onValueChange = { inputValue = it.lowercase().trim() },
                    placeholder = {
                        Text(
                            if (ruleType == "DOMAIN") "facebook.com" else "gambling",
                            color = TextMuted
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Cyan400,
                        unfocusedBorderColor = Surface600,
                        focusedContainerColor = Navy900,
                        unfocusedContainerColor = Navy900,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (inputValue.isNotBlank()) onAdd(ruleType, inputValue) },
                enabled = inputValue.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900)
            ) {
                Text("Block", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        }
    )
}
