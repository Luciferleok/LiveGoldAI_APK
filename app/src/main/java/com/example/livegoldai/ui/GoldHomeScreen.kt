package com.example.livegoldai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.livegoldai.theme.*
import com.example.livegoldai.ui.components.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoldHomeScreen(
    viewModel: GoldViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        SettingsDialog(
            currentApiKey = uiState.apiKey,
            onSaveKey = { newKey -> viewModel.updateApiKey(newKey) },
            onDismiss = { showSettings = false }
        )
    }

    if (uiState.showLotCalculator && uiState.data != null) {
        LotCalculatorDialog(
            initialStopLossPips = uiState.selectedSlPips,
            currentGoldPrice = uiState.data!!.currentPrice,
            onDismiss = { viewModel.closeLotCalculator() }
        )
    }

    if (uiState.showPriceAlertDialog && uiState.data != null) {
        PriceAlertDialog(
            currentPrice = uiState.data!!.currentPrice,
            activeTargetPrice = uiState.priceAlertTarget,
            onSetAlert = { target -> viewModel.setPriceAlert(target) },
            onDismiss = { viewModel.closePriceAlertDialog() }
        )
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBackground),
        containerColor = ObsidianBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GoldPrimary, GoldDark)
                                    )
                                )
                                .border(1.dp, GoldLight, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "FX",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Black,
                                color = ObsidianBackground
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "KALANKAR FX",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SignalBuy)
                                )
                            }
                            Text(
                                text = "By Rudvay Ujjwal Kalankar",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                color = TextGold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val slPips = uiState.data?.tradeSetup?.stopLossPips ?: 90.0
                            viewModel.openLotCalculator(slPips)
                        },
                        modifier = Modifier.testTag("top_lot_calc_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Lot Calculator",
                            tint = GoldLight
                        )
                    }

                    IconButton(
                        onClick = { viewModel.openPriceAlertDialog() },
                        modifier = Modifier.testTag("top_price_alert_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.priceAlertTarget != null) {
                                    Badge(containerColor = if (uiState.isAlertTriggered) SignalSell else GoldPrimary)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Price Alert",
                                tint = if (uiState.priceAlertTarget != null) GoldPrimary else TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { viewModel.loadData(isInitial = false) },
                        modifier = Modifier.testTag("top_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Data",
                            tint = TextPrimary
                        )
                    }

                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ObsidianBackground,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.data == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = GoldPrimary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Analyzing XAU/USD technical indicators...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextSecondary
                            )
                        }
                    }
                }

                uiState.data != null -> {
                    val analysis = uiState.data!!
                    val tabs = listOf("ALL", "TREND", "MOMENTUM", "VOLATILITY", "PIVOTS", "CANDLES")

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp)
                    ) {
                        // Alert Triggered Banner
                        if (uiState.isAlertTriggered) {
                            item(key = "alert_triggered_banner") {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = SignalSell.copy(alpha = 0.2f),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.linearGradient(listOf(SignalSell, GoldPrimary))
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
                                                tint = SignalSell,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "PRICE ALERT TRIGGERED!",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Black,
                                                    color = SignalSell
                                                )
                                                Text(
                                                    text = "Target ($${String.format(Locale.US, "%.2f", uiState.priceAlertTarget ?: 0.0)}) hit! Live Gold: $${String.format(Locale.US, "%.2f", analysis.currentPrice)}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextPrimary
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = { viewModel.dismissAlertBanner() },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Dismiss",
                                                tint = TextSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Live Price Card with timeframes
                        item(key = "price_header") {
                            PriceHeaderCard(
                                analysis = analysis,
                                selectedInterval = uiState.selectedInterval,
                                countdownSeconds = uiState.countdownSeconds,
                                isRefreshing = uiState.isRefreshing,
                                onIntervalSelected = { viewModel.setInterval(it) },
                                onRefreshClick = { viewModel.loadData(isInitial = false) },
                                alertTargetPrice = uiState.priceAlertTarget,
                                isAlertTriggered = uiState.isAlertTriggered,
                                onAlertClick = { viewModel.openPriceAlertDialog() }
                            )
                        }

                        // Actionable Trade Setup Card (Signal, Entry, SL, TP1, TP2, R:R, Copy Plan)
                        item(key = "trade_setup") {
                            TradeSetupCard(
                                setup = analysis.tradeSetup,
                                onOpenCalculator = { slPips -> viewModel.openLotCalculator(slPips) }
                            )
                        }

                        // Hero Consensus Card (Votes breakdown & overall bias)
                        item(key = "hero_consensus") {
                            HeroConsensusCard(analysis = analysis)
                        }

                        // Pro Interactive Candlestick Action Chart (Scrub crosshair, EMA 9/21, BB, Volume, Zoom)
                        item(key = "pro_chart") {
                            ProCandleChart(candles = analysis.recentCandles)
                        }

                        // Global Market Sessions Card (London, NY, Tokyo, Sydney & Golden Overlap)
                        item(key = "market_sessions") {
                            MarketSessionsCard(sessions = analysis.marketSessions)
                        }

                        // Floor Pivot Price Ladder
                        item(key = "pivot_ladder") {
                            PivotLadderCard(
                                currentPrice = analysis.currentPrice,
                                pivotLevels = analysis.pivotLevels
                            )
                        }

                        // Category Filter Tabs
                        item(key = "category_tabs") {
                            ScrollableTabRow(
                                selectedTabIndex = uiState.selectedTab,
                                edgePadding = 0.dp,
                                containerColor = Color.Transparent,
                                contentColor = GoldPrimary,
                                indicator = {},
                                divider = {}
                            ) {
                                tabs.forEachIndexed { index, title ->
                                    val isSelected = uiState.selectedTab == index
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) GoldPrimary else ObsidianSurfaceElevated,
                                        border = if (isSelected) null else CardDefaults.outlinedCardBorder().copy(
                                            brush = Brush.linearGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                                        ),
                                        modifier = Modifier
                                            .padding(end = 8.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.setTab(index) }
                                            .testTag("tab_$title")
                                    ) {
                                        Text(
                                            text = title,
                                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                                            color = if (isSelected) ObsidianBackground else TextSecondary
                                        )
                                    }
                                }
                            }
                        }

                        // Indicator Group Cards based on selected tab
                        val filteredGroups = when (uiState.selectedTab) {
                            1 -> analysis.groups.filter { it.key == "trend" }
                            2 -> analysis.groups.filter { it.key == "momentum" }
                            3 -> analysis.groups.filter { it.key == "volatility" }
                            4 -> analysis.groups.filter { it.key == "sr" }
                            5 -> analysis.groups.filter { it.key == "candlestick" }
                            else -> analysis.groups
                        }

                        items(filteredGroups, key = { it.key }) { group ->
                            IndicatorGroupCard(group = group)
                        }

                        // Attribution & Disclaimer Footer
                        item(key = "footer") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "KALANKAR FX GOLD PRO",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Dedicated to Mr. Rudvay Ujjwal Kalankar",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Experimental analysis engine • Not financial advice",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Text(
                                text = "Unable to load live analysis",
                                style = MaterialTheme.typography.titleMedium,
                                color = SignalSell
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = uiState.errorMessage!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadData(isInitial = true) },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                            ) {
                                Text(text = "Try Again", color = ObsidianBackground)
                            }
                        }
                    }
                }
            }
        }
    }
}

