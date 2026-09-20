package com.example.livegoldai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livegoldai.model.PivotLevels
import com.example.livegoldai.theme.*
import java.util.Locale

@Composable
fun PivotLadderCard(
    currentPrice: Double,
    pivotLevels: PivotLevels,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pivot_ladder_card"),
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
                    text = "FLOOR PIVOT PRICE LADDER",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Live Market Zones",
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = GoldLight
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Ladder items
            LadderRow(label = "R2 Resistance", price = pivotLevels.r2, color = SignalSell, currentPrice = currentPrice)
            LadderRow(label = "R1 Resistance", price = pivotLevels.r1, color = SignalSell.copy(alpha = 0.85f), currentPrice = currentPrice)
            LadderRow(label = "Floor Pivot (P)", price = pivotLevels.pivot, color = GoldPrimary, currentPrice = currentPrice, isPivot = true)
            LadderRow(label = "S1 Support", price = pivotLevels.s1, color = SignalBuy.copy(alpha = 0.85f), currentPrice = currentPrice)
            LadderRow(label = "S2 Support", price = pivotLevels.s2, color = SignalBuy, currentPrice = currentPrice)
        }
    }
}

@Composable
private fun LadderRow(
    label: String,
    price: Double,
    color: Color,
    currentPrice: Double,
    isPivot: Boolean = false
) {
    val distance = currentPrice - price
    val isNear = kotlin.math.abs(distance) < 5.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(if (isPivot) 10.dp else 8.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isPivot) FontWeight.Bold else FontWeight.Medium,
                color = if (isPivot) GoldLight else TextSecondary
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isNear) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = GoldContainer,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = "NEAR",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                        style = MaterialTheme.typography.labelMedium.copy(fontSize = 9.sp),
                        color = GoldLight,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = "$${String.format(Locale.US, "%,.2f", price)}",
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
