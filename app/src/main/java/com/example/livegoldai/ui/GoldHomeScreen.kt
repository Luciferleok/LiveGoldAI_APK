package com.example.livegoldai.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.livegoldai.R
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
                        Image(
                            painter = painterResource(id = R.drawable.img_royal_gold_emblem),
                            contentDescription = "Kalankar FX Gold Royal Logo",
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.5.dp, GoldPrimary, RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "KALANKAR FX GOLD PRO",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = TextPrimary,
                                    letterSpacing = 0.5.sp,
                                    fontSize = 15.sp
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
                                text = "VIP BULLION TERMINAL • By Rudvay Ujjwal Kalankar",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                                color = TextGold,
                                fontWeight = FontWeight.SemiBold
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
                    val tabs = listOf(
                        "🎯 VIP COCKPIT",
                        "🕯️ CANDLE & MTF",
                        "📈 CHART & SMC",
                        "🌍 MACRO & NEWS",
                        "⚡ PRO TRICKS",
                        "📊 32 INDICATORS"
                    )

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

                        // 1. Live Price Card with timeframes
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

                        // 2. Executive 1-Glance Snapshot HUD (Direction, Entry, Seal SL, Target TP1)
                        item(key = "executive_hud") {
                            ExecutiveSummaryBar(
                                analysis = analysis,
                                onTabSelect = { tabIndex -> viewModel.setTab(tabIndex) }
                            )
                        }

                        // 3. Category Filter Tabs (Smooth, Intuitive Navigation)
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

                        // --- TAB 0: 🎯 VIP COCKPIT ---
                        if (uiState.selectedTab == 0) {
                            analysis.nextPrediction?.let { prediction ->
                                item(key = "prediction_oracle") {
                                    PredictionOracleCard(
                                        prediction = prediction,
                                        onOpenCalculator = {
                                            val slPips = analysis.tradeSetup.stopLossPips
                                            viewModel.openLotCalculator(slPips)
                                        }
                                    )
                                }
                            }

                            item(key = "pro_chart_cockpit") {
                                ProCandleChart(candles = analysis.recentCandles)
                            }

                            if (analysis.candleInsight != null && analysis.mtfMatrix != null) {
                                item(key = "candle_mtf_cockpit") {
                                    CandleMtfOracleCard(
                                        candleInsight = analysis.candleInsight!!,
                                        mtfMatrix = analysis.mtfMatrix!!,
                                        tradingTricks = analysis.tradingTricks
                                    )
                                }
                            }
                        }

                        // --- TAB 1: 🕯️ CANDLE & MTF ---
                        if (uiState.selectedTab == 1) {
                            if (analysis.candleInsight != null && analysis.mtfMatrix != null) {
                                item(key = "candle_mtf_page") {
                                    CandleMtfOracleCard(
                                        candleInsight = analysis.candleInsight!!,
                                        mtfMatrix = analysis.mtfMatrix!!,
                                        tradingTricks = analysis.tradingTricks
                                    )
                                }
                            }

                            item(key = "pro_chart_candle") {
                                ProCandleChart(candles = analysis.recentCandles)
                            }
                        }

                        // --- TAB 2: 📈 CHART & SMC ---
                        if (uiState.selectedTab == 2) {
                            item(key = "pro_chart_smc") {
                                ProCandleChart(candles = analysis.recentCandles)
                            }

                            analysis.smartMoney?.let { smc ->
                                item(key = "smart_money_smc_page") {
                                    SmartMoneySmcCard(
                                        smc = smc,
                                        currentPrice = analysis.currentPrice
                                    )
                                }
                            }
                        }

                        // --- TAB 3: 🌍 MACRO & NEWS ---
                        if (uiState.selectedTab == 3) {
                            analysis.macroRadar?.let { radar ->
                                item(key = "macro_news_radar_page") {
                                    MacroNewsRadarCard(radar = radar)
                                }
                            }

                            item(key = "market_sessions_page") {
                                MarketSessionsCard(sessions = analysis.marketSessions)
                            }
                        }

                        // --- TAB 4: ⚡ PRO TRICKS ---
                        if (uiState.selectedTab == 4) {
                            item(key = "trade_setup_tricks") {
                                TradeSetupCard(
                                    setup = analysis.tradeSetup,
                                    onOpenCalculator = { slPips -> viewModel.openLotCalculator(slPips) }
                                )
                            }

                            if (analysis.candleInsight != null && analysis.mtfMatrix != null) {
                                item(key = "tricks_oracle") {
                                    CandleMtfOracleCard(
                                        candleInsight = analysis.candleInsight!!,
                                        mtfMatrix = analysis.mtfMatrix!!,
                                        tradingTricks = analysis.tradingTricks
                                    )
                                }
                            }
                        }

                        // --- TAB 5: 📊 32 INDICATORS ---
                        if (uiState.selectedTab == 5) {
                            item(key = "hero_consensus_ind") {
                                HeroConsensusCard(analysis = analysis)
                            }

                            item(key = "pivot_ladder_ind") {
                                PivotLadderCard(
                                    currentPrice = analysis.currentPrice,
                                    pivotLevels = analysis.pivotLevels
                                )
                            }

                            items(analysis.groups, key = { it.key }) { group ->
                                IndicatorGroupCard(group = group)
                            }
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

