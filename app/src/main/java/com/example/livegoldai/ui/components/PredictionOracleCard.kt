package com.example.livegoldai.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livegoldai.R
import com.example.livegoldai.model.NextPredictionPlaybook
import com.example.livegoldai.model.PredictionOutcomeStatus
import com.example.livegoldai.model.Signal
import com.example.livegoldai.model.TimeframeAccuracyAudit
import com.example.livegoldai.theme.*

@Composable
fun PredictionOracleCard(
    prediction: NextPredictionPlaybook,
    onOpenCalculator: () -> Unit = {},
    logoRes: Int = R.drawable.ic_luxury_gold_logo,
    onLogoClick: () -> Unit = {},
    timeframeAudit: TimeframeAccuracyAudit? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showEnglishDetails by remember { mutableStateOf(false) }
    var selectedTierIndex by remember { mutableIntStateOf(0) }
    var isVoicePlaying by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val signalColor = when (prediction.verdict) {
        Signal.BUY -> NeonGreen
        Signal.SELL -> NeonRed
        Signal.WAIT -> AmberWarning
    }

    val containerBg = when (prediction.verdict) {
        Signal.BUY -> Color(0xFF091610)
        Signal.SELL -> Color(0xFF19090D)
        Signal.WAIT -> Color(0xFF14120B)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("prediction_oracle_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                listOf(
                    GoldPrimary.copy(alpha = 0.9f),
                    signalColor.copy(alpha = 0.7f),
                    ObsidianBorderHighlight,
                    GoldDark
                )
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Luxury Logo, Title & Win Confluence Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = logoRes),
                        contentDescription = "Kalankar Royal Gold Emblem",
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, GoldLight, RoundedCornerShape(10.dp))
                            .clickable { onLogoClick() },
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(signalColor.copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AI PROFIT ORACLE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = GoldLight,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "INSTITUTIONAL PREDICTION & PROTOCOL",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                    }
                }

                // Confluence Win Probability Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = signalColor.copy(alpha = 0.18f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(signalColor, GoldPrimary))
                    )
                ) {
                    Text(
                        text = "${prediction.winProbabilityPercent}% WIN RATE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = signalColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            // Timeframe Accuracy & Last Prediction Result Strip
            timeframeAudit?.let { audit ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ObsidianSurfaceElevated,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(GoldPrimary.copy(alpha = 0.5f), ObsidianBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = SignalBuy,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${audit.timeframe}: ${audit.winRatePercent}% ACCURACY (${audit.winCount}W/${audit.lossCount}L)",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                fontWeight = FontWeight.Black,
                                color = GoldLight
                            )
                        }

                        audit.lastPredictionOutcome?.let { last ->
                            val isWin = last.outcomeStatus == PredictionOutcomeStatus.TP1_HIT ||
                                    last.outcomeStatus == PredictionOutcomeStatus.TP2_HIT
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = (if (isWin) SignalBuy else SignalSell).copy(alpha = 0.18f)
                            ) {
                                Text(
                                    text = if (isWin) "Last: TP Hit 🟢" else "Last: SL Hit 🔴",
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWin) SignalBuy else SignalSell
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Verdict Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = signalColor.copy(alpha = 0.16f),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(
                        listOf(signalColor.copy(alpha = 0.8f), GoldPrimary.copy(alpha = 0.5f))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = prediction.urgencyTag,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = signalColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = prediction.actionHeading,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = signalColor,
                            modifier = Modifier.padding(start = 12.dp)
                        ) {
                            Text(
                                text = prediction.verdict.label,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (prediction.verdict == Signal.BUY) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Exact Trade & Order Execution Tags
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianBackground.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "TRADE TYPE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        prediction.tradeType,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ObsidianBackground.copy(alpha = 0.7f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = signalColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        "ORDER TYPE",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = TextMuted
                                    )
                                    Text(
                                        prediction.orderExecutionType,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = signalColor
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Entry & Stop Loss (SEAL) Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Entry Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KAHAN ENTER KAREIN",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = prediction.recommendedEntryZone,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "Pullback / Limit Zone",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = TextGold
                        )
                    }
                }

                // Stop Loss ("SEAL") Box
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF220E12),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(NeonRed, ObsidianBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = null,
                                tint = NeonRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SEAL (STOP LOSS)",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = NeonRed.copy(alpha = 0.9f),
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = prediction.stopLossLevel,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Black,
                            color = NeonRed
                        )
                        Text(
                            text = "Capital Invalidation Line",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = NeonRed.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // SL (SEAL) RATIONALE BANNER - Directly answering user's question "Seal Kahan Hai Iska Aur Kyun"
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1B1115),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(NeonRed.copy(alpha = 0.5f), ObsidianBorder))
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = null,
                        tint = NeonRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔒 SEAL DETAIL: ${prediction.stopLossRationale}",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 11.sp,
                        color = Color(0xFFFFCDD2),
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Take Profit (TP1, TP2, TP3) Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // TP1
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(NeonGreen.copy(alpha = 0.6f), ObsidianBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TP 1 (50%)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(prediction.takeProfit1, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = NeonGreen)
                        Text("Book half & SL to Entry", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                    }
                }

                // TP2
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(GoldPrimary.copy(alpha = 0.6f), ObsidianBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TP 2 (30%)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(prediction.takeProfit2, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = GoldPrimary)
                        Text("Trail remainder", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                    }
                }

                // TP3 (Runner)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(Color(0xFF80D8FF).copy(alpha = 0.6f), ObsidianBorder))
                    )
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFF80D8FF), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("TP 3 (20%)", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(if (prediction.takeProfit3.isNotEmpty()) prediction.takeProfit3 else "Runner", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.ExtraBold, color = Color(0xFF80D8FF))
                        Text("Moonbag target", style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = TextMuted)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step-by-Step Action Box ("EXACT ENTRY & NO-TRADE PROTOCOL")
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF131722),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = Brush.horizontalGradient(listOf(GoldPrimary.copy(alpha = 0.5f), ObsidianBorder))
                ),
                modifier = Modifier.fillMaxWidth()
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ENTRY & NO-TRADE PROTOCOL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Black,
                                color = GoldLight
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isVoicePlaying = !isVoicePlaying
                                    Toast.makeText(
                                        context,
                                        if (isVoicePlaying) "🔊 Playing Hindi Trade Instructions..." else "🔇 Audio Stopped",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (isVoicePlaying) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                    contentDescription = "Voice Guide",
                                    tint = if (isVoicePlaying) NeonGreen else GoldPrimary
                                )
                            }

                            TextButton(
                                onClick = { showEnglishDetails = !showEnglishDetails },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (showEnglishDetails) "हिंदी गाइड" else "Full Plan",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GoldPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. KAHAN ENTRY KAREIN (Exact Setup in Hindi)
                    if (prediction.whereToEnterHindi.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F2618),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(NeonGreen.copy(alpha = 0.8f), GoldPrimary.copy(alpha = 0.4f)))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "KAHAN ENTRY KAREIN (EXACT TRIGGER)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonGreen
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = prediction.whereToEnterHindi.removePrefix("✅ KAHAN ENTRY KAREIN: "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 17.sp,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // 2. KAHAN BILKUL ENTRY NAHI KARNI (Strict Avoid in Hindi)
                    if (prediction.whereToAvoidHindi.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF280F14),
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(NeonRed.copy(alpha = 0.8f), Color(0xFF5D121F)))
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    Icons.Default.Cancel,
                                    contentDescription = null,
                                    tint = NeonRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "KAHAN BILKUL ENTRY NAHI KARNI (TRAP ZONE)",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NeonRed
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = prediction.whereToAvoidHindi.removePrefix("❌ KAHAN BILKUL ENTRY NAHI KARNI: "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFFFCDD2),
                                        fontWeight = FontWeight.SemiBold,
                                        lineHeight = 17.sp,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Optional Full Strategy Guide (English or Detailed Hindi)
                    if (showEnglishDetails) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ObsidianBackground.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "INSTITUTIONAL STRATEGY OVERVIEW",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldLight
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = prediction.whatToDoEnglish,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    lineHeight = 18.sp,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    // Voice Audio Bubble
                    if (isVoicePlaying && prediction.hindiAudioAdvice.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SignalBuyBg,
                            border = CardDefaults.outlinedCardBorder().copy(
                                brush = Brush.horizontalGradient(listOf(NeonGreen, GoldPrimary))
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.GraphicEq, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = prediction.hindiAudioAdvice,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Divider(color = ObsidianBorder)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⏱️ Horizon: ${prediction.timeHorizon}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                        Text(
                            text = "🛡️ ${prediction.riskManagementRule}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Interactive Account Balance Matrix (Safe Lot Size & Dollar Risk vs Reward)
            if (prediction.accountTierMatrix.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = ObsidianSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ACCOUNT EQUITY & EXACT LOT RECOMMENDATION",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Black,
                                    color = GoldLight,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Account Balance Tabs ($100, $500, $1k, $5k, $10k)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            prediction.accountTierMatrix.forEachIndexed { idx, tier ->
                                val isSelected = selectedTierIndex == idx
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTierIndex = idx },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) GoldPrimary else ObsidianSurfaceElevated,
                                    border = if (isSelected) null else CardDefaults.outlinedCardBorder()
                                ) {
                                    Text(
                                        text = tier.balanceLabel.replace(" Account", "").replace(" VIP", ""),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                        color = if (isSelected) Color.Black else TextMuted,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Details for selected tier
                        val activeTier = prediction.accountTierMatrix.getOrNull(selectedTierIndex)
                            ?: prediction.accountTierMatrix[0]

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Recommended Lot", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                Text(activeTier.safeLotSize, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = GoldLight)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Risk If SL (Seal) Hits", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                Text(activeTier.riskAmountDollars, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = NeonRed)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Profit At TP1", style = MaterialTheme.typography.labelSmall, color = TextMuted, fontSize = 10.sp)
                                Text(activeTier.rewardTp1Dollars, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.ExtraBold, color = NeonGreen)
                            }
                        }
                    }
                }
            }

            // 4-Step Execution Checklist
            if (prediction.executionRules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianSurfaceCard,
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.verticalGradient(listOf(ObsidianBorderHighlight, ObsidianBorder))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Text(
                            text = "TRADE EXECUTION CHECKLIST (4 CONFIRMATIONS)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black,
                            color = GoldLight,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        prediction.executionRules.forEach { rule ->
                            Row(
                                modifier = Modifier.padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = rule,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: One-Tap Copy Profit Plan & Lot Calculator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText(
                            "Gold Signal Plan",
                            "👑 KALANKAR FX GOLD PRO • OFFICIAL SIGNAL PLAYBOOK\n" +
                                "Pair: XAU/USD (Gold Spot)\n" +
                                "Verdict: ${prediction.verdict.label} (${prediction.urgencyTag})\n" +
                                "Trade Type: ${prediction.tradeType}\n" +
                                "Order: ${prediction.orderExecutionType}\n" +
                                "Entry Zone: ${prediction.recommendedEntryZone}\n" +
                                "Stop Loss (Seal): ${prediction.stopLossLevel}\n" +
                                "Why SL: ${prediction.stopLossRationale}\n" +
                                "TP 1: ${prediction.takeProfit1} (Book 50% & SL to Entry)\n" +
                                "TP 2: ${prediction.takeProfit2} (Book 30%)\n" +
                                "TP 3: ${prediction.takeProfit3} (Moonbag 20%)\n" +
                                "Win Rate: ${prediction.winProbabilityPercent}%\n" +
                                "Curated by Rudvay Ujjwal Kalankar"
                        )
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Full Profit Plan copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_profit_plan_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ObsidianSurfaceElevated,
                        contentColor = GoldPrimary
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.horizontalGradient(listOf(GoldPrimary, ObsidianBorderHighlight))
                    )
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Profit Plan", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onOpenCalculator,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_lot_calc_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldPrimary,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Lot & Pip Calc", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
