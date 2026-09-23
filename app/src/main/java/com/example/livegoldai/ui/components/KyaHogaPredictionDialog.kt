package com.example.livegoldai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.livegoldai.model.GoldAnalysisResult
import com.example.livegoldai.model.PredictionOutcomeStatus
import com.example.livegoldai.model.Signal
import com.example.livegoldai.theme.*
import java.util.Locale

@Composable
fun KyaHogaPredictionDialog(
    analysis: GoldAnalysisResult,
    onOpenLotCalculator: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val prediction = analysis.nextPrediction
    val candleInsight = analysis.candleInsight
    val tradeSetup = analysis.tradeSetup

    // Probabilities calculation
    val baseProb = prediction?.winProbabilityPercent ?: tradeSetup.confidencePercent
    val isBuy = analysis.overallSignal == Signal.BUY
    val isSell = analysis.overallSignal == Signal.SELL

    val bullishProb: Int
    val sidewaysProb: Int
    val bearishProb: Int

    if (isBuy) {
        bullishProb = baseProb.coerceIn(65, 94)
        val remainder = 100 - bullishProb
        sidewaysProb = (remainder * 0.65).toInt().coerceAtLeast(4)
        bearishProb = (remainder - sidewaysProb).coerceAtLeast(3)
    } else if (isSell) {
        bearishProb = baseProb.coerceIn(65, 94)
        val remainder = 100 - bearishProb
        sidewaysProb = (remainder * 0.65).toInt().coerceAtLeast(4)
        bullishProb = (remainder - sidewaysProb).coerceAtLeast(3)
    } else {
        sidewaysProb = 62
        bullishProb = 21
        bearishProb = 17
    }

    val primaryColor = when {
        isBuy -> SignalBuy
        isSell -> SignalSell
        else -> SignalWait
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("kya_hoga_prediction_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = ObsidianSurfaceCard,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(
                    listOf(GoldPrimary, primaryColor.copy(alpha = pulseGlow), ObsidianBorderHighlight, GoldDark)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.2f))
                                .border(1.5.dp, primaryColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "AGLA KYA HOGA?",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = GoldLight,
                                    letterSpacing = 0.8.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor.copy(alpha = 0.2f),
                                    border = CardDefaults.outlinedCardBorder().copy(
                                        brush = Brush.horizontalGradient(listOf(primaryColor, GoldLight))
                                    )
                                ) {
                                    Text(
                                        text = "${prediction?.winProbabilityPercent ?: baseProb}% CONFIDENCE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = primaryColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "AI Future Prediction & Multi-Scenario Possibility",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // 1. Direct Forecast Big Answer Card
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ObsidianSurfaceElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(
                                listOf(primaryColor.copy(alpha = 0.8f), ObsidianBorderHighlight)
                            )
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🔮 AI MAIN VERDICT / BHAWISHYAVANI",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = GoldLight,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = prediction?.urgencyTag ?: "HIGH CONVICTION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = primaryColor
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = when {
                                    isBuy -> "🟢 GOLD MEIN +${tradeSetup.takeProfit2Pips.toInt()} PIPS KI UPWARD RALLY KI PURI SAMBHAVNA HAI!"
                                    isSell -> "🔴 GOLD MEIN -${tradeSetup.takeProfit2Pips.toInt()} PIPS KE BEARISH DROP KI SAMBHAVNA HAI!"
                                    else -> "🟡 GOLD MEIN ABHI CONSOLIDATION / RANGE-BOUND MOVE RAHEGA!"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                lineHeight = 22.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = prediction?.actionHeading ?: (
                                    if (isBuy) "Buyers are accumulating heavily at support. Target $${String.format(Locale.US, "%.2f", tradeSetup.takeProfit2)}"
                                    else "Sellers are dominant under resistance. Target $${String.format(Locale.US, "%.2f", tradeSetup.takeProfit2)}"
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGold,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (prediction?.whatToDoHindi?.isNotBlank() == true) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = ObsidianBackground,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "💡", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = prediction.whatToDoHindi,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextPrimary,
                                            fontSize = 11.sp,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 1.5 Timeframe Accuracy & Last Prediction Outcome Banner
                    analysis.timeframeAudit?.let { audit ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = ObsidianSurfaceCard,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.6f), ObsidianBorderHighlight))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "🏆", fontSize = 14.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${audit.timeframe} ACCURACY: ${audit.winRatePercent}% WIN RATE",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Black,
                                            color = GoldLight
                                        )
                                    }
                                    Text(
                                        text = "${audit.winCount} Won / ${audit.lossCount} Lost",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                        color = SignalBuy
                                    )
                                }

                                audit.lastPredictionOutcome?.let { last ->
                                    val isWin = last.outcomeStatus == PredictionOutcomeStatus.TP1_HIT ||
                                            last.outcomeStatus == PredictionOutcomeStatus.TP2_HIT
                                    val outcomeColor = if (isWin) SignalBuy else SignalSell
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = outcomeColor.copy(alpha = 0.12f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (isWin) "✅ Pichhla Signal: Target Hit (+${last.pipsResult} Pips)" else "⚠️ Pichhla Signal: SL Hit (${last.pipsResult} Pips)",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = outcomeColor
                                                )
                                                Text(
                                                    text = last.timeAgo,
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                                    color = TextMuted
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Kyu hua: ${last.whyItHappenedHindi}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                                color = TextPrimary
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = "Aage kya seekha: ${last.lessonLearnedHindi}",
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, lineHeight = 15.sp),
                                                color = GoldLight
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Multi-Scenario Possibility Meter (Kiski Kitni Sambhavna Hai)
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = ObsidianSurfaceElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.linearGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = GoldPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "POSSIBILITY METER (KISKI KITNI SAMBHAVNA HAI)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = GoldLight,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Bullish Probability Bar
                            ProbabilityItemRow(
                                title = "🚀 Bullish Rally (Upward Move)",
                                hindiDetail = "Price higher levels test karega",
                                percentage = bullishProb,
                                barColor = SignalBuy,
                                isPrimary = isBuy
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Sideways Probability Bar
                            ProbabilityItemRow(
                                title = "⏸️ Sideways Range (No Big Move)",
                                hindiDetail = "Price ek tight band mein move karega",
                                percentage = sidewaysProb,
                                barColor = SignalWait,
                                isPrimary = !isBuy && !isSell
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Bearish Probability Bar
                            ProbabilityItemRow(
                                title = "🔻 Bearish Drop (Downward Move)",
                                hindiDetail = "Price lower support break karega",
                                percentage = bearishProb,
                                barColor = SignalSell,
                                isPrimary = isSell
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Composite Visual Stack Bar
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(bullishProb.toFloat().coerceAtLeast(1f))
                                            .fillMaxHeight()
                                            .background(SignalBuy)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(sidewaysProb.toFloat().coerceAtLeast(1f))
                                            .fillMaxHeight()
                                            .background(SignalWait)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(bearishProb.toFloat().coerceAtLeast(1f))
                                            .fillMaxHeight()
                                            .background(SignalSell)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "🟢 Buy $bullishProb%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SignalBuy
                                    )
                                    Text(
                                        text = "🟡 Range $sidewaysProb%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SignalWait
                                    )
                                    Text(
                                        text = "🔴 Sell $bearishProb%",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SignalSell
                                    )
                                }
                            }
                        }
                    }

                    // 3. Current Live Status vs Future Target Glance
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianSurfaceElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "🔴 ABHI KYA CHAL RAHA HAI VS AGLA TARGET",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = GoldLight
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Abhi Ka Live Price",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", analysis.currentPrice)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "${if (analysis.changeAmount >= 0) "+" else ""}${String.format(Locale.US, "%.2f", analysis.changeAmount)} (${String.format(Locale.US, "%.2f", analysis.changePercent)}%)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (analysis.changeAmount >= 0) SignalBuy else SignalSell,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(GoldPrimary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingFlat,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = "Agla Expected Target",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        text = "$${String.format(Locale.US, "%.2f", tradeSetup.takeProfit1)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = primaryColor
                                    )
                                    Text(
                                        text = "TP2: $${String.format(Locale.US, "%.2f", tradeSetup.takeProfit2)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            if (candleInsight != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ObsidianBackground,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🕯️", fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Next Candle Forecast: ${candleInsight.nextCandleForecast}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontSize = 10.sp,
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 4. Action Playbook (Entry, SL, TP)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianSurfaceElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(primaryColor, GoldPrimary))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🎯 AAPKO KYA KARNA CHAHIYE (ACTION PLAN)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = GoldLight
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = primaryColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "R:R ${tradeSetup.riskRewardRatio}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = primaryColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                ActionItemPill(
                                    label = "ENTRY ZONE",
                                    value = "$${String.format(Locale.US, "%.2f", tradeSetup.entryPrice)}",
                                    color = GoldLight,
                                    modifier = Modifier.weight(1f)
                                )
                                ActionItemPill(
                                    label = "STOP LOSS",
                                    value = "$${String.format(Locale.US, "%.2f", tradeSetup.stopLoss)}",
                                    color = SignalSell,
                                    modifier = Modifier.weight(1f)
                                )
                                ActionItemPill(
                                    label = "TARGET TP1",
                                    value = "$${String.format(Locale.US, "%.2f", tradeSetup.takeProfit1)}",
                                    color = SignalBuy,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // 1-Tap Lot Calculator Button
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenLotCalculator(tradeSetup.stopLossPips)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = GoldPrimary.copy(alpha = 0.15f)
                                ),
                                border = CardDefaults.outlinedCardBorder().copy(
                                    brush = Brush.horizontalGradient(listOf(GoldPrimary, GoldLight))
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Calculate Safe Lot Size For My Account",
                                    color = GoldLight,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // 5. Invalidation Warning Alert
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SignalSell.copy(alpha = 0.12f),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(SignalSell, ObsidianBorder))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = SignalSell,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Kab Ye Prediction Cancel Hogi? (Invalidation)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = SignalSell
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Agar candle $${String.format(Locale.US, "%.2f", tradeSetup.stopLoss)} ke paar close ho jaye, to setup turant invalid ho jayega. Stop Loss lagana mandatory hai.",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }

                    // 6. Why This Prediction (3 Technical Reasons)
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = ObsidianSurfaceElevated,
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = Brush.horizontalGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "🔬 KYUN HOGA AISA? (3 MAIN REASONS)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = GoldLight
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            ReasonRow(
                                number = "1",
                                text = "Smart Money: Liquidity grab ke baad institutional order block se strong wick rejection mili hai."
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ReasonRow(
                                number = "2",
                                text = "Trend Confluence: ${analysis.buyCount} out of ${analysis.totalGroups} major indicator groups ${analysis.overallSignal.name} bias dikha rahe hain."
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            ReasonRow(
                                number = "3",
                                text = "Multi-Timeframe: M15 aur H1 charts par momentum expansion shuru ho chuka hai."
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action: Got It / Close
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("close_prediction_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "I UNDERSTAND THE PROBABILITIES • GOT IT",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ProbabilityItemRow(
    title: String,
    hindiDetail: String,
    percentage: Int,
    barColor: Color,
    isPrimary: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isPrimary) FontWeight.Black else FontWeight.Bold,
                    color = if (isPrimary) Color.White else TextSecondary
                )
                Text(
                    text = hindiDetail,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = barColor.copy(alpha = if (isPrimary) 0.25f else 0.15f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(barColor, barColor))
                )
            ) {
                Text(
                    text = "$percentage% CHANCE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = barColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = ObsidianBorder
        )
    }
}

@Composable
private fun ActionItemPill(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ObsidianBackground,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(color.copy(alpha = 0.5f), ObsidianBorder))
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
    }
}

@Composable
private fun ReasonRow(
    number: String,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(GoldPrimary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GoldLight
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontSize = 10.sp,
            color = TextSecondary,
            lineHeight = 14.sp
        )
    }
}
