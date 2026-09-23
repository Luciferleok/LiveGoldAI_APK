package com.example.livegoldai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livegoldai.model.CandleBar
import com.example.livegoldai.theme.*
import java.util.Locale
import kotlin.math.max
import kotlin.math.min

@Composable
fun ProCandleChart(
    candles: List<CandleBar>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    var candleCount by remember { mutableIntStateOf(30) }
    var showSuperTrend by remember { mutableStateOf(true) }
    var showEma by remember { mutableStateOf(true) }
    var showBb by remember { mutableStateOf(false) }
    var showVolume by remember { mutableStateOf(true) }
    var showVwap by remember { mutableStateOf(true) }

    var selectedCandleIndex by remember { mutableStateOf<Int?>(null) }

    val displayCandles = candles.takeLast(candleCount)
    val maxPrice = displayCandles.maxOfOrNull { it.high } ?: 1.0
    val minPrice = displayCandles.minOfOrNull { it.low } ?: 0.0
    val priceSpan = max(maxPrice - minPrice, 0.5)

    val maxVolume = displayCandles.maxOfOrNull { it.volume ?: 1000.0 } ?: 1000.0

    // Currently inspected candle or the latest candle
    val activeCandle = selectedCandleIndex?.let { idx ->
        if (idx in displayCandles.indices) displayCandles[idx] else null
    } ?: displayCandles.lastOrNull()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pro_candle_chart"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Bar: Title & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INTERACTIVE PRICE ACTION",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Black,
                        color = GoldLight,
                        letterSpacing = 0.8.sp
                    )
                }

                // Candle count selector chips
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(15, 30, 45).forEach { count ->
                        val isSelected = candleCount == count
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isSelected) GoldPrimary else ObsidianSurfaceElevated,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .pointerInput(count) {
                                    detectTapGestures {
                                        candleCount = count
                                        selectedCandleIndex = null
                                    }
                                }
                        ) {
                            Text(
                                text = "${count}B",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) ObsidianBackground else TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Dynamic Inspector Bar (HUD)
            if (activeCandle != null) {
                val isBull = activeCandle.close >= activeCandle.open
                val candleDiff = activeCandle.close - activeCandle.open
                val pips = candleDiff * 10.0

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianSurfaceElevated,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = activeCandle.datetime,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = TextMuted
                            )
                            Text(
                                text = "${if (pips >= 0) "+" else ""}${String.format(Locale.US, "%.1f", pips)} Pips",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Bold,
                                color = if (isBull) SignalBuy else SignalSell
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HudItem(label = "O", value = String.format(Locale.US, "%.1f", activeCandle.open))
                            HudItem(label = "H", value = String.format(Locale.US, "%.1f", activeCandle.high))
                            HudItem(label = "L", value = String.format(Locale.US, "%.1f", activeCandle.low))
                            HudItem(label = "C", value = String.format(Locale.US, "%.1f", activeCandle.close), color = if (isBull) SignalBuy else SignalSell)
                            activeCandle.vwap?.let {
                                HudItem(label = "VWAP", value = String.format(Locale.US, "%.1f", it), color = Color(0xFFFF9100))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Interactive Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(displayCandles) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                val count = displayCandles.size
                                if (count > 0) {
                                    val slotW = size.width / count
                                    val idx = (offset.x / slotW).toInt().coerceIn(0, count - 1)
                                    selectedCandleIndex = idx
                                }
                            },
                            onDrag = { change, _ ->
                                val count = displayCandles.size
                                if (count > 0) {
                                    val slotW = size.width / count
                                    val idx = (change.position.x / slotW).toInt().coerceIn(0, count - 1)
                                    selectedCandleIndex = idx
                                }
                            },
                            onDragEnd = {
                                // keep or reset on drag end
                            },
                            onDragCancel = {}
                        )
                    }
                    .pointerInput(displayCandles) {
                        detectTapGestures { offset ->
                            val count = displayCandles.size
                            if (count > 0) {
                                val slotW = size.width / count
                                val idx = (offset.x / slotW).toInt().coerceIn(0, count - 1)
                                selectedCandleIndex = if (selectedCandleIndex == idx) null else idx
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val count = displayCandles.size
                    if (count == 0) return@Canvas

                    val slotWidth = w / count
                    val candleWidth = slotWidth * 0.65f

                    // 4 Horizontal Guideline reference levels
                    for (i in 1..4) {
                        val y = h * (i / 5f)
                        val priceLevel = maxPrice - (priceSpan * (i / 5f))
                        drawLine(
                            color = ObsidianBorder.copy(alpha = 0.5f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1f
                        )
                    }

                    // Helper to convert price to Y pixel coordinate
                    fun priceToY(p: Double): Float {
                        return ((maxPrice - p) / priceSpan * h).toFloat().coerceIn(0f, h)
                    }

                    // Draw Volume Bars at bottom if enabled
                    if (showVolume) {
                        val maxVolHeight = h * 0.22f
                        displayCandles.forEachIndexed { i, candle ->
                            val vol = candle.volume ?: 500.0
                            val barH = ((vol / maxVolume) * maxVolHeight).toFloat().coerceIn(2f, maxVolHeight)
                            val centerX = (i * slotWidth) + (slotWidth / 2f)
                            val isBull = candle.close >= candle.open
                            val volColor = if (isBull) SignalBuy.copy(alpha = 0.25f) else SignalSell.copy(alpha = 0.25f)
                            drawRect(
                                color = volColor,
                                topLeft = Offset(centerX - (candleWidth / 2f), h - barH),
                                size = Size(candleWidth, barH)
                            )
                        }
                    }

                    // Draw Bollinger Bands if enabled
                    if (showBb) {
                        val upperPath = Path()
                        val lowerPath = Path()
                        var hasFirst = false

                        displayCandles.forEachIndexed { i, c ->
                            if (c.bbUpper != null && c.bbLower != null) {
                                val x = (i * slotWidth) + (slotWidth / 2f)
                                val uy = priceToY(c.bbUpper)
                                val ly = priceToY(c.bbLower)
                                if (!hasFirst) {
                                    upperPath.moveTo(x, uy)
                                    lowerPath.moveTo(x, ly)
                                    hasFirst = true
                                } else {
                                    upperPath.lineTo(x, uy)
                                    lowerPath.lineTo(x, ly)
                                }
                            }
                        }

                        if (hasFirst) {
                            drawPath(upperPath, color = Color(0xFFB388FF).copy(alpha = 0.65f), style = Stroke(width = 1.2f))
                            drawPath(lowerPath, color = Color(0xFFB388FF).copy(alpha = 0.65f), style = Stroke(width = 1.2f))
                        }
                    }

                    // Draw EMA 9 (Gold) and EMA 21 (Cyan) if enabled
                    if (showEma) {
                        val ema9Path = Path()
                        val ema21Path = Path()
                        var first9 = false
                        var first21 = false

                        displayCandles.forEachIndexed { i, c ->
                            val x = (i * slotWidth) + (slotWidth / 2f)
                            if (c.ema9 != null) {
                                val y9 = priceToY(c.ema9)
                                if (!first9) {
                                    ema9Path.moveTo(x, y9)
                                    first9 = true
                                } else {
                                    ema9Path.lineTo(x, y9)
                                }
                            }
                            if (c.ema21 != null) {
                                val y21 = priceToY(c.ema21)
                                if (!first21) {
                                    ema21Path.moveTo(x, y21)
                                    first21 = true
                                } else {
                                    ema21Path.lineTo(x, y21)
                                }
                            }
                        }

                        if (first9) {
                            drawPath(ema9Path, color = GoldLight, style = Stroke(width = 1.8f))
                        }
                        if (first21) {
                            drawPath(ema21Path, color = Color(0xFF00E5FF), style = Stroke(width = 1.6f))
                        }
                    }

                    // Draw SuperTrend (10, 3.0) Line & Trail
                    if (showSuperTrend) {
                        for (i in 0 until count) {
                            val stVal = displayCandles[i].superTrend ?: continue
                            val isBull = displayCandles[i].close >= stVal
                            val stColor = if (isBull) NeonGreen else NeonRed
                            val leftX = i * slotWidth
                            val rightX = (i + 1) * slotWidth
                            val y = priceToY(stVal)
                            drawLine(
                                color = stColor,
                                start = Offset(leftX, y),
                                end = Offset(rightX, y),
                                strokeWidth = 2.5f
                            )
                            if (i > 0) {
                                val prevSt = displayCandles[i - 1].superTrend
                                if (prevSt != null) {
                                    val prevY = priceToY(prevSt)
                                    drawLine(
                                        color = stColor.copy(alpha = 0.6f),
                                        start = Offset(leftX, prevY),
                                        end = Offset(leftX, y),
                                        strokeWidth = 1.5f
                                    )
                                }
                            }
                        }
                    }

                    // Draw VWAP (Volume-Weighted Average Price) Line (Amber)
                    if (showVwap) {
                        val vwapPath = Path()
                        var firstVwap = false
                        displayCandles.forEachIndexed { i, c ->
                            val x = (i * slotWidth) + (slotWidth / 2f)
                            if (c.vwap != null) {
                                val vy = priceToY(c.vwap)
                                if (!firstVwap) {
                                    vwapPath.moveTo(x, vy)
                                    firstVwap = true
                                } else {
                                    vwapPath.lineTo(x, vy)
                                }
                            }
                        }
                        if (firstVwap) {
                            drawPath(
                                vwapPath,
                                color = Color(0xFFFF9100),
                                style = Stroke(width = 2.0f)
                            )
                        }
                    }

                    // Draw Candlesticks (Wick + Body)
                    displayCandles.forEachIndexed { index, candle ->
                        val isBullish = candle.close >= candle.open
                        val candleColor = if (isBullish) SignalBuy else SignalSell

                        val centerX = (index * slotWidth) + (slotWidth / 2f)
                        val highY = priceToY(candle.high)
                        val lowY = priceToY(candle.low)
                        val openY = priceToY(candle.open)
                        val closeY = priceToY(candle.close)

                        val bodyTop = min(openY, closeY)
                        val bodyBottom = max(openY, closeY)
                        val bodyHeight = max(bodyBottom - bodyTop, 2.5f)

                        // Draw Wick
                        drawLine(
                            color = candleColor,
                            start = Offset(centerX, highY),
                            end = Offset(centerX, lowY),
                            strokeWidth = 1.5f
                        )

                        // Draw Body
                        drawRect(
                            color = candleColor,
                            topLeft = Offset(centerX - (candleWidth / 2f), bodyTop),
                            size = Size(candleWidth, bodyHeight)
                        )
                    }

                    // Draw Crosshair on selected index
                    selectedCandleIndex?.let { selIdx ->
                        if (selIdx in 0 until count) {
                            val c = displayCandles[selIdx]
                            val centerX = (selIdx * slotWidth) + (slotWidth / 2f)
                            val closeY = priceToY(c.close)

                            // Vertical crosshair line
                            drawLine(
                                color = GoldPrimary,
                                start = Offset(centerX, 0f),
                                end = Offset(centerX, h),
                                strokeWidth = 1.5f
                            )

                            // Horizontal price level line
                            drawLine(
                                color = GoldPrimary.copy(alpha = 0.7f),
                                start = Offset(0f, closeY),
                                end = Offset(w, closeY),
                                strokeWidth = 1f
                            )

                            // Pointer dot
                            drawCircle(
                                color = GoldPrimary,
                                radius = 4f,
                                center = Offset(centerX, closeY)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Controls Row: Overlays Toggles & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Toggles for SuperTrend, EMA, BB, Volume, VWAP
                Row(
                    modifier = Modifier.weight(1f, fill = false).horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChartToggleChip(
                        label = "SUPERTREND",
                        isActive = showSuperTrend,
                        activeColor = NeonGreen,
                        onClick = { showSuperTrend = !showSuperTrend }
                    )
                    ChartToggleChip(
                        label = "EMA 9/21",
                        isActive = showEma,
                        activeColor = GoldLight,
                        onClick = { showEma = !showEma }
                    )
                    ChartToggleChip(
                        label = "VWAP",
                        isActive = showVwap,
                        activeColor = Color(0xFFFF9100),
                        onClick = { showVwap = !showVwap }
                    )
                    ChartToggleChip(
                        label = "BB 2.0",
                        isActive = showBb,
                        activeColor = Color(0xFFB388FF),
                        onClick = { showBb = !showBb }
                    )
                    ChartToggleChip(
                        label = "VOL",
                        isActive = showVolume,
                        activeColor = TextSecondary,
                        onClick = { showVolume = !showVolume }
                    )
                }

                Text(
                    text = "Touch to scrub",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun HudItem(label: String, value: String, color: Color = TextPrimary) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = TextMuted
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ChartToggleChip(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isActive) activeColor.copy(alpha = 0.15f) else ObsidianSurfaceElevated,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (isActive) listOf(activeColor, activeColor.copy(alpha = 0.4f))
                else listOf(ObsidianBorderHighlight, ObsidianBorder)
            )
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .pointerInput(Unit) {
                detectTapGestures { onClick() }
            }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = if (isActive) activeColor else TextMuted
        )
    }
}
