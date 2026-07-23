package com.tgm.tgmc.feature.child.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tgm.tgmc.core.data.remote.EducationalContent
import com.tgm.tgmc.core.data.remote.StoreItem
import com.tgm.tgmc.navigation.TgmcRoutes
import com.tgm.tgmc.ui.theme.*

// ═══════════════════════════════════════════════════════════════════
// Child Main Screen — Bottom Navigation Host
// ═══════════════════════════════════════════════════════════════════

@Composable
fun ChildMainScreen(
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: TgmcRoutes.Child.HOME

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Navy800,
                contentColor = TextSecondary,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == TgmcRoutes.Child.HOME,
                    onClick = {
                        if (currentRoute != TgmcRoutes.Child.HOME) {
                            navController.navigate(TgmcRoutes.Child.HOME) {
                                popUpTo(TgmcRoutes.Child.HOME) { inclusive = true }
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = childNavColors()
                )
                NavigationBarItem(
                    selected = currentRoute == TgmcRoutes.Child.LEARN,
                    onClick = {
                        if (currentRoute != TgmcRoutes.Child.LEARN) {
                            navController.navigate(TgmcRoutes.Child.LEARN) {
                                popUpTo(TgmcRoutes.Child.HOME)
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.School, contentDescription = "Learn") },
                    label = { Text("Learn") },
                    colors = childNavColors()
                )
                NavigationBarItem(
                    selected = currentRoute == TgmcRoutes.Child.STORE,
                    onClick = {
                        if (currentRoute != TgmcRoutes.Child.STORE) {
                            navController.navigate(TgmcRoutes.Child.STORE) {
                                popUpTo(TgmcRoutes.Child.HOME)
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Storefront, contentDescription = "Store") },
                    label = { Text("Store") },
                    colors = childNavColors()
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = TgmcRoutes.Child.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(TgmcRoutes.Child.HOME) {
                ChildHomeScreen()
            }
            composable(TgmcRoutes.Child.LEARN) {
                ChildLearnScreen()
            }
            composable(TgmcRoutes.Child.STORE) {
                ChildStoreScreen()
            }
        }
    }
}

@Composable
private fun childNavColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = Navy900,
    selectedTextColor = Cyan400,
    indicatorColor = Cyan400,
    unselectedIconColor = TextMuted,
    unselectedTextColor = TextMuted
)

// ═══════════════════════════════════════════════════════════════════
// Learn Screen — Educational Hub (C4–C7)
// WhatsApp-style Status ring at top, then tabbed feed below
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildLearnScreen(
    viewModel: ChildContentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("All", "Posts", "Reels", "Videos")

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, tint = Cyan400, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Educational Hub", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Cyan400)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(uiState.error!!, color = ErrorRed, style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900)
                    ) { Text("Retry") }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // ── C4: WhatsApp-style Status Row ─────────────────────
                if (uiState.statuses.isNotEmpty()) {
                    item {
                        StatusRow(statuses = uiState.statuses)
                    }
                }

                // ── Tab Row for C5/C6/C7 ─────────────────────────────
                item {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Navy900,
                        edgePadding = 16.dp,
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
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // ── Filtered Feed ────────────────────────────────────
                val filteredFeed = when (selectedTab) {
                    1 -> uiState.feed.filter { it.type == "POST" }
                    2 -> uiState.feed.filter { it.type == "REEL" }
                    3 -> uiState.feed.filter { it.type == "VIDEO" }
                    else -> uiState.feed
                }

                if (filteredFeed.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inbox, contentDescription = null, tint = TextMuted, modifier = Modifier.size(40.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No ${if (selectedTab == 0) "content" else tabs[selectedTab].lowercase()} yet.", color = TextMuted)
                            }
                        }
                    }
                } else {
                    items(filteredFeed, key = { it.id }) { item ->
                        when (item.type) {
                            "REEL" -> ReelCard(item)
                            "VIDEO" -> VideoCard(item)
                            else -> PostCard(item)
                        }
                    }
                }
            }
        }
    }
}

// ── C4: Status Row (WhatsApp-style circular thumbnails) ───────────

@Composable
fun StatusRow(statuses: List<EducationalContent>) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            "  Today's Updates",
            style = MaterialTheme.typography.labelMedium.copy(color = TextMuted),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(statuses, key = { it.id }) { status ->
                StatusBubble(status)
            }
        }
        HorizontalDivider(
            color = Surface700,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun StatusBubble(status: EducationalContent) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp).clickable { /* Open status viewer */ }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(listOf(Cyan400, SuccessGreen)),
                    shape = CircleShape
                )
                .padding(3.dp)
                .clip(CircleShape)
                .background(Surface700),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Cyan400,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = status.title,
            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

// ── C5: Post Card (Instagram-style educational post) ──────────────

@Composable
fun PostCard(item: EducationalContent) {
    Surface(
        color = Surface800,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = Cyan400.copy(alpha = 0.15f),
                    shape = CircleShape,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Cyan400, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("TGM-C Education", style = MaterialTheme.typography.labelMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                    Text(
                        item.category ?: "General",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                    )
                }
            }

            // Media area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Surface700),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Image, contentDescription = null, tint = TextMuted.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
            }

            // Caption
            Column(modifier = Modifier.padding(14.dp)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold))
                if (!item.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.description, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary), maxLines = 3, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

// ── C6: Reel Card (TikTok-style short-form vertical video) ────────

@Composable
fun ReelCard(item: EducationalContent) {
    Surface(
        color = Surface800,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.height(140.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reel thumbnail
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
                    .background(Surface700),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = Cyan400, modifier = Modifier.size(40.dp))
                // Duration badge
                if (item.durationSecs != null) {
                    Surface(
                        color = Navy900.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                    ) {
                        Text(
                            formatDuration(item.durationSecs),
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Reel info
            Column(
                modifier = Modifier.weight(1f).padding(14.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Surface(color = WarningAmber.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                    Text(
                        "REEL",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall.copy(color = WarningAmber, fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    item.category ?: "General",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextMuted)
                )
            }
        }
    }
}

// ── C7: Video Card (longer-form educational video) ────────────────

@Composable
fun VideoCard(item: EducationalContent) {
    Surface(
        color = Surface800,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Column {
            // Video preview area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Surface700),
                contentAlignment = Alignment.Center
            ) {
                // Play button overlay
                Surface(
                    color = Navy900.copy(alpha = 0.6f),
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = TextPrimary, modifier = Modifier.size(32.dp))
                    }
                }

                // Duration badge
                if (item.durationSecs != null) {
                    Surface(
                        color = Navy900.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(10.dp)
                    ) {
                        Text(
                            formatDuration(item.durationSecs),
                            style = MaterialTheme.typography.labelSmall.copy(color = TextPrimary),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Category badge
                Surface(
                    color = Cyan400.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Text(
                        item.category ?: "General",
                        style = MaterialTheme.typography.labelSmall.copy(color = Navy900, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Title + description
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary, fontWeight = FontWeight.Bold)
                )
                if (!item.description.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        item.description,
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Store Screen — Educational Shopping (C9)
// ═══════════════════════════════════════════════════════════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChildStoreScreen(
    viewModel: ChildContentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Navy900,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Storefront, contentDescription = null, tint = Cyan400, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Educational Store", color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy800)
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Cyan400)
            }
        } else if (uiState.error != null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(uiState.error!!, color = ErrorRed)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadData() },
                        colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900)
                    ) { Text("Retry") }
                }
            }
        } else if (uiState.storeItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = TextMuted, modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Store is empty right now.", color = TextMuted, style = MaterialTheme.typography.bodyMedium)
                    Text("Check back later for new items!", color = TextMuted, style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.storeItems, key = { it.id }) { item ->
                    StoreItemCard(item, viewModel)
                }
            }
        }
    }
}

@Composable
fun StoreItemCard(item: StoreItem, viewModel: ChildContentViewModel) {
    Surface(
        color = Surface800,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Product image area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(
                        Brush.verticalGradient(listOf(Surface700, Surface800))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = TextMuted.copy(alpha = 0.4f), modifier = Modifier.size(52.dp))
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    item.name,
                    style = MaterialTheme.typography.titleSmall.copy(color = TextPrimary, fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    item.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, lineHeight = 16.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))

                // Price + request button
                Button(
                    onClick = { 
                        viewModel.requestPurchase(item.id) 
                    },
                    modifier = Modifier.fillMaxWidth().height(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = Navy900),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Ask · \$${String.format("%.2f", item.price)}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// Utility
// ═══════════════════════════════════════════════════════════════════

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}:${s.toString().padStart(2, '0')}" else "0:${s.toString().padStart(2, '0')}"
}
