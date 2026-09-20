package com.example.livegoldai.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
fun MiniCandleChart(
    candles: List<CandleBar>,
    modifier: Modifier = Modifier
) {
    if (candles.isEmpty()) return

    val displayCandles = candles.takeLast(24)
    val maxPrice = displayCandles.maxOfOrNull { it.high } ?: 1.0
    val minPrice = displayCandles.minOfOrNull { it.low } ?: 0.0
    val priceSpan = max(maxPrice - minPrice, 0.5)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mini_candle_chart"),
        shape = RoundedCornerShape(20.dp),
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "XAU/USD PRICE ACTION",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "High: $${String.format(Locale.US, "%.2f", maxPrice)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = GoldLight,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                val w = size.width
                val h = size.height
                val count = displayCandles.size
                if (count == 0) return@Canvas

                val slotWidth = w / count
                val candleWidth = slotWidth * 0.65f

                // Draw 3 horizontal guideline reference levels
                for (i in 1..3) {
                    val y = h * (i / 4f)
                    drawLine(
                        color = ObsidianBorder.copy(alpha = 0.6f),
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                // Draw Candles
                displayCandles.forEachIndexed { index, candle ->
                    val isBullish = candle.close >= candle.open
                    val candleColor = if (isBullish) SignalBuy else SignalSell

                    val centerX = (index * slotWidth) + (slotWidth / 2f)

                    // Normalize Y coordinates (0 is top)
                    val highY = ((maxPrice - candle.high) / priceSpan * h).toFloat().coerceIn(0f, h)
                    val lowY = ((maxPrice - candle.low) / priceSpan * h).toFloat().coerceIn(0f, h)
                    val openY = ((maxPrice - candle.open) / priceSpan * h).toFloat().coerceIn(0f, h)
                    val closeY = ((maxPrice - candle.close) / priceSpan * h).toFloat().coerceIn(0f, h)

                    val bodyTop = min(openY, closeY)
                    val bodyBottom = max(openY, closeY)
                    val bodyHeight = max(bodyBottom - bodyTop, 2.5f)

                    // Draw Wick line
                    drawLine(
                        color = candleColor,
                        start = Offset(centerX, highY),
                        end = Offset(centerX, lowY),
                        strokeWidth = 1.5f
                    )

                    // Draw Body box
                    drawRect(
                        color = candleColor,
                        topLeft = Offset(centerX - (candleWidth / 2f), bodyTop),
                        size = Size(candleWidth, bodyHeight)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Past 24 Candles",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = TextMuted
                )
                Text(
                    text = "Low: $${String.format(Locale.US, "%.2f", minPrice)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted
                )
            }
        }
    }
}
