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
import com.example.livegoldai.localization.AppLanguage
import com.example.livegoldai.localization.LocalAppLanguage
import com.example.livegoldai.model.PivotLevels
import com.example.livegoldai.theme.*
import java.util.Locale

@Composable
fun PivotLadderCard(
    currentPrice: Double,
    pivotLevels: PivotLevels,
    modifier: Modifier = Modifier
) {
    val currentLanguage = LocalAppLanguage.current

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
                    text = when (currentLanguage) {
                        AppLanguage.ENGLISH -> "FLOOR PIVOT PRICE LADDER"
                        AppLanguage.HINDI -> "फ्लोर पिवट प्राइस लैडर"
                        AppLanguage.MARATHI -> "फ्लोर पिव्हट प्राईस लॅडर"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    letterSpacing = 1.sp
                )
                Text(
                    text = when (currentLanguage) {
                        AppLanguage.ENGLISH -> "Live Market Zones"
                        AppLanguage.HINDI -> "लाइव मार्केट ज़ोन"
                        AppLanguage.MARATHI -> "लाईव्ह मार्केट झोन्स"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                    color = GoldLight
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            val r2Label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "R2 Resistance"
                AppLanguage.HINDI -> "R2 रेसिस्टेंस"
                AppLanguage.MARATHI -> "R2 रेझिस्टन्स"
            }
            val r1Label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "R1 Resistance"
                AppLanguage.HINDI -> "R1 रेसिस्टेंस"
                AppLanguage.MARATHI -> "R1 रेझिस्टन्स"
            }
            val pivotLabel = when (currentLanguage) {
                AppLanguage.ENGLISH -> "Floor Pivot (P)"
                AppLanguage.HINDI -> "फ्लोर पिवट (P)"
                AppLanguage.MARATHI -> "फ्लोर पिव्हट (P)"
            }
            val s1Label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "S1 Support"
                AppLanguage.HINDI -> "S1 सपोर्ट"
                AppLanguage.MARATHI -> "S1 सपोर्ट"
            }
            val s2Label = when (currentLanguage) {
                AppLanguage.ENGLISH -> "S2 Support"
                AppLanguage.HINDI -> "S2 सपोर्ट"
                AppLanguage.MARATHI -> "S2 सपोर्ट"
            }

            // Ladder items
            LadderRow(label = r2Label, price = pivotLevels.r2, color = SignalSell, currentPrice = currentPrice)
            LadderRow(label = r1Label, price = pivotLevels.r1, color = SignalSell.copy(alpha = 0.85f), currentPrice = currentPrice)
            LadderRow(label = pivotLabel, price = pivotLevels.pivot, color = GoldPrimary, currentPrice = currentPrice, isPivot = true)
            LadderRow(label = s1Label, price = pivotLevels.s1, color = SignalBuy.copy(alpha = 0.85f), currentPrice = currentPrice)
            LadderRow(label = s2Label, price = pivotLevels.s2, color = SignalBuy, currentPrice = currentPrice)
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
