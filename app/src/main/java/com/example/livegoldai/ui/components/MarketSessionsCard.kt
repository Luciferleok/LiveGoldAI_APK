package com.example.livegoldai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.livegoldai.localization.AppLanguage
import com.example.livegoldai.localization.LocalAppLanguage
import com.example.livegoldai.model.MarketSession
import com.example.livegoldai.theme.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun MarketSessionsCard(
    sessions: List<MarketSession>,
    modifier: Modifier = Modifier
) {
    if (sessions.isEmpty()) return

    val hasGoldenOverlap = sessions.any { it.isGoldenOverlap }
    val currentUtcTime = rememberUtcTime()
    val currentLanguage = LocalAppLanguage.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_sessions_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurfaceCard),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (hasGoldenOverlap) listOf(GoldPrimary.copy(alpha = 0.5f), ObsidianBorder)
                else listOf(ObsidianBorderHighlight, ObsidianBorder)
            )
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Title and UTC clock
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Market Hours",
                        tint = GoldPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ENGLISH -> "GLOBAL GOLD SESSIONS"
                            AppLanguage.HINDI -> "ग्लोबल गोल्ड ट्रेडिंग सेशन्स"
                            AppLanguage.MARATHI -> "जागतिक गोल्ड ट्रेडिंग सेशन्स"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = GoldLight,
                        letterSpacing = 0.8.sp
                    )
                }

                Text(
                    text = "$currentUtcTime UTC",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextGold
                )
            }

            // Golden Overlap Alert Banner
            if (hasGoldenOverlap) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = GoldDark.copy(alpha = 0.35f),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = Brush.linearGradient(listOf(GoldPrimary, GoldDark))
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Overlap",
                            tint = GoldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = when (currentLanguage) {
                                AppLanguage.ENGLISH -> "LONDON / NY OVERLAP ACTIVE • Peak Gold Trading Volume"
                                AppLanguage.HINDI -> "लंदन / न्यूयॉर्क ओवरलैप एक्टिव • सर्वाधिक ट्रेडिंग वॉल्यूम"
                                AppLanguage.MARATHI -> "लंडन / न्यूयॉर्क ओव्हरलॅप सक्रिय • सर्वाधिक ट्रेडिंग व्हॉल्यूम"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            color = GoldLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Sessions grid (2 columns)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sessions.chunked(2).forEach { rowSessions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowSessions.forEach { session ->
                            SessionItemBox(
                                session = session,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowSessions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionItemBox(
    session: MarketSession,
    modifier: Modifier = Modifier
) {
    val currentLanguage = LocalAppLanguage.current
    val statusColor = if (session.isOpen) SignalBuy else TextMuted
    val statusBg = if (session.isOpen) SignalBuyBg else ObsidianSurfaceElevated

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ObsidianSurfaceElevated,
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(
                if (session.isOpen) listOf(SignalBuy.copy(alpha = 0.3f), ObsidianBorder)
                else listOf(ObsidianBorderHighlight, ObsidianBorder)
            )
        ),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = session.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Status Badge
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusBg
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (session.isOpen) {
                                when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "OPEN"
                                    AppLanguage.HINDI -> "ओपन"
                                    AppLanguage.MARATHI -> "सुरू"
                                }
                            } else {
                                when (currentLanguage) {
                                    AppLanguage.ENGLISH -> "CLOSED"
                                    AppLanguage.HINDI -> "बंद"
                                    AppLanguage.MARATHI -> "बंद"
                                }
                            },
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = session.timeWindowUtc,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Vol: ${session.volatilityLevel}",
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                color = if (session.isOpen) GoldLight else TextMuted
            )
        }
    }
}

private fun rememberUtcTime(): String {
    return try {
        val now = ZonedDateTime.now(java.time.ZoneOffset.UTC)
        now.format(DateTimeFormatter.ofPattern("HH:mm"))
    } catch (_: Exception) {
        "12:00"
    }
}
