package com.example.livegoldai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.HourglassEmpty
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
import com.example.livegoldai.model.GroupAnalysis
import com.example.livegoldai.model.IndicatorItem
import com.example.livegoldai.model.Signal
import com.example.livegoldai.theme.*

@Composable
fun IndicatorGroupCard(
    group: GroupAnalysis,
    modifier: Modifier = Modifier
) {
    val currentLanguage = LocalAppLanguage.current
    val verdictColor = when (group.verdict) {
        Signal.BUY -> SignalBuy
        Signal.SELL -> SignalSell
        Signal.WAIT -> SignalWait
    }

    val verdictBg = when (group.verdict) {
        Signal.BUY -> SignalBuyContainer
        Signal.SELL -> SignalSellContainer
        Signal.WAIT -> SignalWaitContainer
    }

    val localizedGroupTitle = when (group.key.lowercase()) {
        "trend" -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "TREND & MOMENTUM"
            AppLanguage.HINDI -> "ट्रेंड मोमेंटम (ट्रेंड की दिशा)"
            AppLanguage.MARATHI -> "ट्रेंड मोमेंटम (दिशा व वेग)"
        }
        "smart_money", "smc" -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "SMART MONEY & LIQUIDITY"
            AppLanguage.HINDI -> "स्मार्ट मनी व लिक्विडिटी"
            AppLanguage.MARATHI -> "स्मार्ट मनी व लिक्विडीटी"
        }
        "volatility" -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "VOLATILITY & BREAKOUT"
            AppLanguage.HINDI -> "वोलैटिलिटी व ब्रेकआउट"
            AppLanguage.MARATHI -> "व्होलॅटिलिटी व ब्रेकआऊट"
        }
        "volume", "order_flow" -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "VOLUME & ORDER FLOW"
            AppLanguage.HINDI -> "वॉल्यूम व ऑर्डर फ्लो"
            AppLanguage.MARATHI -> "व्हॉल्यूम व ऑर्डर फ्लो"
        }
        "levels", "sr" -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "SUPPORT & RESISTANCE"
            AppLanguage.HINDI -> "सपोर्ट और रेजिस्टेंस लेवल्स"
            AppLanguage.MARATHI -> "सपोर्ट आणि रेझिस्टन्स लेव्हल्स"
        }
        else -> group.title.uppercase()
    }

    val localizedVerdict = when (group.verdict) {
        Signal.BUY -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "BUY"
            AppLanguage.HINDI -> "BUY (खरीद)"
            AppLanguage.MARATHI -> "BUY (खरेदी)"
        }
        Signal.SELL -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "SELL"
            AppLanguage.HINDI -> "SELL (बिक्री)"
            AppLanguage.MARATHI -> "SELL (विक्री)"
        }
        Signal.WAIT -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "WAIT"
            AppLanguage.HINDI -> "WAIT (इंतज़ार)"
            AppLanguage.MARATHI -> "WAIT (वाट)"
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("group_card_${group.key}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    verdictColor.copy(alpha = 0.35f),
                    ObsidianBorderHighlight,
                    ObsidianBorder
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Group Title & Verdict Badge Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(verdictColor)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = localizedGroupTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = verdictBg,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(verdictColor, verdictColor.copy(alpha = 0.3f)))
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (group.verdict) {
                                Signal.BUY -> Icons.Default.ArrowUpward
                                Signal.SELL -> Icons.Default.ArrowDownward
                                Signal.WAIT -> Icons.Default.HourglassEmpty
                            },
                            contentDescription = null,
                            tint = verdictColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = localizedVerdict,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Black,
                            color = verdictColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Indicator Rows
            group.indicators.forEachIndexed { index, indicator ->
                IndicatorRowItem(indicator = indicator, currentLanguage = currentLanguage)
                if (index < group.indicators.size - 1) {
                    HorizontalDivider(
                        color = ObsidianBorder.copy(alpha = 0.5f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun IndicatorRowItem(
    indicator: IndicatorItem,
    currentLanguage: AppLanguage
) {
    val sigColor = when (indicator.signal) {
        Signal.BUY -> SignalBuy
        Signal.SELL -> SignalSell
        Signal.WAIT -> SignalWait
    }

    val localizedSignal = when (indicator.signal) {
        Signal.BUY -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "BUY"
            AppLanguage.HINDI -> "BUY"
            AppLanguage.MARATHI -> "BUY"
        }
        Signal.SELL -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "SELL"
            AppLanguage.HINDI -> "SELL"
            AppLanguage.MARATHI -> "SELL"
        }
        Signal.WAIT -> when (currentLanguage) {
            AppLanguage.ENGLISH -> "WAIT"
            AppLanguage.HINDI -> "WAIT"
            AppLanguage.MARATHI -> "WAIT"
        }
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = indicator.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = sigColor.copy(alpha = 0.15f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.linearGradient(listOf(sigColor.copy(alpha = 0.8f), sigColor.copy(alpha = 0.2f)))
                )
            ) {
                Text(
                    text = localizedSignal,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    fontWeight = FontWeight.Bold,
                    color = sigColor
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = indicator.detail,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = indicator.valueDisplay,
                style = MaterialTheme.typography.labelMedium,
                color = GoldLight,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
