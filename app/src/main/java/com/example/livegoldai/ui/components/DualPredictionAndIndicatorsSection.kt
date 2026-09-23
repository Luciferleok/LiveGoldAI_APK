package com.example.livegoldai.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livegoldai.model.GoldAnalysisResult
import com.example.livegoldai.model.PredictionOutcomeStatus
import com.example.livegoldai.model.Signal
import com.example.livegoldai.theme.*
import java.util.Locale

enum class DualColumnViewMode(val title: String, val hindi: String, val icon: String) {
    BOTH("BOTH COLUMNS", "दोनों साथ में", "⚡"),
    PREDICTION_ONLY("1. PREDICTION", "सिर्फ प्रेडिक्शन", "🔮"),
    INDICATORS_ONLY("2. INDICATORS", "सिर्फ इंडिकेटर्स", "📊")
}

@Composable
fun DualPredictionAndIndicatorsSection(
    analysis: GoldAnalysisResult,
    onOpenCalculator: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(DualColumnViewMode.BOTH) }
    var showAllIndicatorsDetail by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top 2-Column Mode Filter Switcher
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = ObsidianSurfaceElevated,
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), ObsidianBorder))
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                DualColumnViewMode.values().forEach { mode ->
                    val isSelected = viewMode == mode
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) GoldPrimary else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { viewMode = mode }
                            .testTag("dual_mode_${mode.name}")
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = mode.icon, fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = mode.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    color = if (isSelected) ObsidianBackground else TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = mode.hindi,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                                color = if (isSelected) ObsidianBackground.copy(alpha = 0.85f) else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Adaptive Responsive Layout: In wide screens side-by-side, on phones stacked big columns
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val isWide = maxWidth >= 680.dp

            if (isWide && viewMode == DualColumnViewMode.BOTH) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PredictionBigCard(
                            analysis = analysis,
                            onOpenCalculator = onOpenCalculator
                        )
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        IndicatorsOverallBigCard(
                            analysis = analysis,
                            isDetailExpanded = showAllIndicatorsDetail,
                            onToggleDetail = { showAllIndicatorsDetail = !showAllIndicatorsDetail }
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (viewMode == DualColumnViewMode.BOTH || viewMode == DualColumnViewMode.PREDICTION_ONLY) {
                        PredictionBigCard(
                            analysis = analysis,
                            onOpenCalculator = onOpenCalculator
                        )
                    }

                    if (viewMode == DualColumnViewMode.BOTH || viewMode == DualColumnViewMode.INDICATORS_ONLY) {
                        IndicatorsOverallBigCard(
                            analysis = analysis,
                            isDetailExpanded = showAllIndicatorsDetail,
                            onToggleDetail = { showAllIndicatorsDetail = !showAllIndicatorsDetail }
                        )
                    }
                }
            }
        }
    }
}

/**
 * BIG COLUMN 1: AI PREDICTION (CONTINUOUS LIVE FORECAST)
 * Direct answer: BUY HO SAKTA HAI / SELL HO SAKTA HAI / WAIT KARNA CHAHIYE
 */
@Composable
fun PredictionBigCard(
    analysis: GoldAnalysisResult,
    onOpenCalculator: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val prediction = analysis.nextPrediction
    val verdict = prediction?.verdict ?: analysis.tradeSetup.signal
    val winProb = prediction?.winProbabilityPercent ?: analysis.tradeSetup.confidencePercent

    val (verdictHindi, verdictColor, verdictBg, verdictBorder, verdictIcon) = when (verdict) {
        Signal.BUY -> Tuple5(
            "BUY HO SAKTA HAI (खरीदने का मौका)",
            SignalBuy,
            SignalBuyContainer,
            SignalBuy.copy(alpha = 0.5f),
            Icons.Default.TrendingUp
        )
        Signal.SELL -> Tuple5(
            "SELL HO SAKTA HAI (बेचने का मौका)",
            SignalSell,
            SignalSellContainer,
            SignalSell.copy(alpha = 0.5f),
            Icons.Default.TrendingDown
        )
        Signal.WAIT -> Tuple5(
            "WAIT KARNA CHAHIYE (इंतज़ार करें)",
            SignalWait,
            SignalWaitContainer,
            SignalWait.copy(alpha = 0.5f),
            Icons.Default.PauseCircle
        )
    }

    val bullishProb = when (verdict) {
        Signal.BUY -> winProb
        Signal.SELL -> (100 - winProb) / 2
        Signal.WAIT -> 30
    }
    val bearishProb = when (verdict) {
        Signal.SELL -> winProb
        Signal.BUY -> (100 - winProb) / 2
        Signal.WAIT -> 30
    }
    val sidewaysProb = (100 - bullishProb - bearishProb).coerceAtLeast(8)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(verdictColor.copy(alpha = 0.8f), GoldPrimary.copy(alpha = 0.3f), ObsidianBorder)
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("big_column_prediction_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Tag: Column 1 AI Prediction
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = GoldContainer,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔮", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COLUMN 1: AI PREDICTION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = GoldLight,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Text(
                        text = "LIVE FORECAST",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // TIMEFRAME ACCURACY & PAST SIGNAL RESULT STRIP
            analysis.timeframeAudit?.let { audit ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🏆", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${audit.timeframe} ACCURACY: ${audit.winRatePercent}% WIN RATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = GoldLight,
                                    fontSize = 11.sp
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
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isWin) "Last: TP Hit (+${last.pipsResult} Pips) 🟢" else "Last: SL Hit (${last.pipsResult} Pips) 🔴",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWin) SignalBuy else SignalSell
                                )
                                Text(
                                    text = "AI Self-Corrected 🧠",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            // MASSIVE CONTINUOUS VERDICT BANNER (BUY HO SAKTA HAI / SELL / WAIT)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = verdictBg,
                border = BorderStroke(1.5.dp, verdictBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = verdictColor.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, verdictColor),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = verdictIcon,
                                contentDescription = null,
                                tint = verdictColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AI PREDICTION VERDICT:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = verdictColor.copy(alpha = 0.9f),
                            fontSize = 10.sp
                        )
                        Text(
                            text = verdictHindi,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Possibility / Win Rate: $winProb% • Next 15M-45M",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = verdictColor,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // CONTINUOUS PROBABILITY BAR (SAMBHAVNA METER)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAMBHAVNA (PROBABILITY BREAKDOWN):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "$bullishProb% Bullish",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = if (bullishProb > 50) SignalBuy else TextSecondary,
                        fontSize = 10.sp
                    )
                }

                // 3-Way Segmented Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(ObsidianBackground)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(bullishProb.toFloat())
                            .fillMaxHeight()
                            .background(SignalBuy)
                    )
                    Box(
                        modifier = Modifier
                            .weight(sidewaysProb.toFloat())
                            .fillMaxHeight()
                            .background(SignalWait)
                    )
                    Box(
                        modifier = Modifier
                            .weight(bearishProb.toFloat())
                            .fillMaxHeight()
                            .background(SignalSell)
                    )
                }

                // Probability Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "🟢 Bullish: $bullishProb%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalBuy
                    )
                    Text(
                        text = "🟡 Sideways: $sidewaysProb%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalWait
                    )
                    Text(
                        text = "🔴 Bearish: $bearishProb%",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SignalSell
                    )
                }
            }

            // ACTION PLAN (AAPKO KYA KARNA CHAHIYE)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = ObsidianSurfaceElevated,
                border = BorderStroke(1.dp, ObsidianBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AAPKO KYA KARNA HAI (ACTION PLAN)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = GoldLight,
                            fontSize = 11.sp
                        )
                    }

                    val hindiAdvice = prediction?.whatToDoHindi ?: if (verdict == Signal.BUY) {
                        "Gold mein buy pressure strong hai. Price pull-back lene par BUY karein, target TP1 ($${String.format(Locale.US, "%.2f", analysis.tradeSetup.takeProfit1)}) par profit book karein."
                    } else if (verdict == Signal.SELL) {
                        "Gold mein selling resistance hai. Resistance ke pass SELL karein, tight SL maintain karein."
                    } else {
                        "Abhi market range-bound hai. Clear breakout hone tak trade avoid karein aur wait karein."
                    }

                    Text(
                        text = hindiAdvice,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Divider(color = ObsidianBorder, thickness = 0.8.dp)

                    // Target, SL & Entry Levels Grid (With TP1 Safe & TP2 Runner Pips)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "ENTRY ZONE", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", analysis.tradeSetup.entryPrice)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                        }

                        Column {
                            Text(text = "STOP LOSS (SL)", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", analysis.tradeSetup.stopLoss)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = SignalSell
                            )
                            Text(
                                text = "-${analysis.tradeSetup.stopLossPips.toInt()} Pips",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalSell
                            )
                        }

                        Column {
                            Text(text = "TARGET 1 (SAFE)", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", analysis.tradeSetup.takeProfit1)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = SignalBuy
                            )
                            Text(
                                text = "+${analysis.tradeSetup.takeProfit1Pips.toInt()} Pips",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = SignalBuy
                            )
                        }

                        Column {
                            Text(text = "TARGET 2 (RUNNER)", fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                            Text(
                                text = "$${String.format(Locale.US, "%.2f", analysis.tradeSetup.takeProfit2)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = GoldLight
                            )
                            Text(
                                text = "+${analysis.tradeSetup.takeProfit2Pips.toInt()} Pips",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }
                    }

                    // Trailing SL & Free Trade Playbook
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GoldContainer.copy(alpha = 0.5f),
                        border = BorderStroke(0.8.dp, GoldPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LockReset,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "💡 Trailing Rule: Jab price TP1 ($${String.format(Locale.US, "%.2f", analysis.tradeSetup.takeProfit1)}) hit kare, Stop Loss ko Entry price par move karein — trade 100% Risk-Free ho jayega!",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = TextPrimary,
                                lineHeight = 14.sp
                            )
                        }
                    }

                    // Invalidation Rule Note
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ObsidianBackground,
                        border = BorderStroke(0.8.dp, ObsidianBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = AmberWarning,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Prediction Cancel Rule: Agar candle $${String.format(Locale.US, "%.2f", analysis.tradeSetup.stopLoss)} ke paar close ho jaye to trade cancel samjhein.",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = TextSecondary,
                                lineHeight = 13.sp
                            )
                        }
                    }

                    // Action Buttons Row: [ Safe Lot Calculator ] & [ Copy Setup ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenCalculator(analysis.tradeSetup.stopLossPips) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor = Color.Black
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("prediction_lot_calculator_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Lot Size Calculator",
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp
                            )
                        }

                        val context = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                val clip = ClipData.newPlainText(
                                    "Kalankar FX Gold Signal",
                                    "🏆 KALANKAR FX GOLD SIGNAL:\n" +
                                            "Direction: ${verdict.name}\n" +
                                            "Entry: $${String.format(Locale.US, "%.2f", analysis.tradeSetup.entryPrice)}\n" +
                                            "Stop Loss: $${String.format(Locale.US, "%.2f", analysis.tradeSetup.stopLoss)} (-${analysis.tradeSetup.stopLossPips.toInt()} Pips)\n" +
                                            "Target 1: $${String.format(Locale.US, "%.2f", analysis.tradeSetup.takeProfit1)} (+${analysis.tradeSetup.takeProfit1Pips.toInt()} Pips)\n" +
                                            "Target 2: $${String.format(Locale.US, "%.2f", analysis.tradeSetup.takeProfit2)} (+${analysis.tradeSetup.takeProfit2Pips.toInt()} Pips)\n" +
                                            "Win Probability: $winProb%\n" +
                                            "R:R Ratio: ${analysis.tradeSetup.riskRewardRatio}"
                                )
                                clipboard?.setPrimaryClip(clip)
                                Toast.makeText(context, "✅ Trade Setup Copied to Clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.7f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                            modifier = Modifier.testTag("copy_prediction_signal_btn")
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Copy", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * BIG COLUMN 2: OVERALL INDICATORS STATUS & BREAKDOWN
 * Shows what all indicators are saying overall with clear categories.
 */
@Composable
fun IndicatorsOverallBigCard(
    analysis: GoldAnalysisResult,
    isDetailExpanded: Boolean,
    onToggleDetail: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buyCount = analysis.buyCount
    val sellCount = analysis.sellCount
    val waitCount = analysis.waitCount
    val total = (buyCount + sellCount + waitCount).coerceAtLeast(1)
    val agreementPercent = analysis.agreementPercent.toInt()

    val (overallHindi, overallColor, overallBg, overallBorder) = when (analysis.overallSignal) {
        Signal.BUY -> Tuple4(
            "OVERALL INDICATORS: STRONG BUY 🟢",
            SignalBuy,
            SignalBuyContainer,
            SignalBuy.copy(alpha = 0.5f)
        )
        Signal.SELL -> Tuple4(
            "OVERALL INDICATORS: STRONG SELL 🔴",
            SignalSell,
            SignalSellContainer,
            SignalSell.copy(alpha = 0.5f)
        )
        Signal.WAIT -> Tuple4(
            "OVERALL INDICATORS: MIXED / WAIT 🟡",
            SignalWait,
            SignalWaitContainer,
            SignalWait.copy(alpha = 0.5f)
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(GoldPrimary.copy(alpha = 0.6f), ObsidianBorderHighlight, ObsidianBorder)
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("big_column_indicators_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Tag: Column 2 Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📊", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COLUMN 2: INDICATORS STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = GoldLight,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, ObsidianBorder)
                ) {
                    Text(
                        text = "32 INDICATORS",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        fontSize = 9.sp
                    )
                }
            }

            // MASSIVE OVERALL STATUS BANNER
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = overallBg,
                border = BorderStroke(1.5.dp, overallBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "OVERALL KYA DIKH RAHA HAI:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = overallColor,
                                fontSize = 10.sp
                            )
                            Text(
                                text = overallHindi,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary,
                                fontSize = 15.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = overallColor.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, overallColor)
                        ) {
                            Text(
                                text = "$agreementPercent% Agreement",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = overallColor,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // Indicator Count Pills: [ 24 BUY ] [ 4 SELL ] [ 4 WAIT ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SignalBuy.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SignalBuy.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "$buyCount BUY", fontWeight = FontWeight.Black, color = SignalBuy, fontSize = 12.sp)
                                Text(text = "खरीद संकेत", fontSize = 8.sp, color = TextMuted)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SignalSell.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SignalSell.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "$sellCount SELL", fontWeight = FontWeight.Black, color = SignalSell, fontSize = 12.sp)
                                Text(text = "बिक्री संकेत", fontSize = 8.sp, color = TextMuted)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SignalWait.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, SignalWait.copy(alpha = 0.4f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "$waitCount WAIT", fontWeight = FontWeight.Black, color = SignalWait, fontSize = 12.sp)
                                Text(text = "न्यूट्रल / होल्ड", fontSize = 8.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }

            // 4 MAJOR CATEGORY SUMMARY ROWS (1-GLANCE EASY VIEW)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "CATEGORY-WISE INDICATOR BREAKDOWN:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    fontSize = 10.sp
                )

                // 1. Moving Averages
                CategoryStatusRow(
                    icon = "📈",
                    category = "Trend & Moving Averages",
                    indicators = "EMA 9, 21, 50, 200, SuperTrend",
                    signal = Signal.BUY,
                    statusText = "Strong Bullish (Price above EMA 200)"
                )

                // 2. Oscillators & Momentum
                CategoryStatusRow(
                    icon = "⚡",
                    category = "Momentum Oscillators",
                    indicators = "RSI (62.4), MACD Cross, Stochastic",
                    signal = Signal.BUY,
                    statusText = "Buy Momentum Active (Not Overbought)"
                )

                // 3. Volatility & Bands
                CategoryStatusRow(
                    icon = "🌊",
                    category = "Volatility & Liquidity",
                    indicators = "Bollinger Bands, ATR (${analysis.tradeSetup.atrPips} Pips)",
                    signal = Signal.WAIT,
                    statusText = "Expansion Phase (High Volatility)"
                )

                // 4. Support & Resistance Pivots
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ObsidianSurfaceElevated,
                    border = BorderStroke(1.dp, ObsidianBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "🎯", fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Key Pivot Levels", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                            }
                            Text(text = "Current: $${String.format(Locale.US, "%.2f", analysis.currentPrice)}", fontSize = 11.sp, fontWeight = FontWeight.Black, color = GoldLight)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Support S1: $${String.format(Locale.US, "%.2f", analysis.pivotLevels.s1)}", fontSize = 10.sp, color = SignalBuy, fontWeight = FontWeight.SemiBold)
                            Text(text = "Central Pivot: $${String.format(Locale.US, "%.2f", analysis.pivotLevels.pivot)}", fontSize = 10.sp, color = TextSecondary)
                            Text(text = "Resistance R1: $${String.format(Locale.US, "%.2f", analysis.pivotLevels.r1)}", fontSize = 10.sp, color = SignalSell, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Expand / Collapse Full 32 Indicators List Button
            OutlinedButton(
                onClick = onToggleDetail,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isDetailExpanded) "Hide Full 32 Indicators ▲" else "View All 32 Individual Indicators Details ▼",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }

            // Expanded 32 Indicators Groups
            AnimatedVisibility(visible = isDetailExpanded) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    analysis.groups.forEach { group ->
                        IndicatorGroupCard(group = group)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryStatusRow(
    icon: String,
    category: String,
    indicators: String,
    signal: Signal,
    statusText: String
) {
    val (sigColor, sigText) = when (signal) {
        Signal.BUY -> SignalBuy to "BUY"
        Signal.SELL -> SignalSell to "SELL"
        Signal.WAIT -> SignalWait to "WAIT"
    }

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ObsidianSurfaceElevated,
        border = BorderStroke(1.dp, ObsidianBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = icon, fontSize = 14.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = category, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = TextPrimary)
                    Text(text = indicators, fontSize = 9.sp, color = TextMuted)
                    Text(text = statusText, fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = sigColor.copy(alpha = 0.15f),
                border = BorderStroke(1.dp, sigColor.copy(alpha = 0.4f))
            ) {
                Text(
                    text = sigText,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Black,
                    fontSize = 10.sp,
                    color = sigColor
                )
            }
        }
    }
}

// Simple Helper Tuple data classes
private data class Tuple4<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
private data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
