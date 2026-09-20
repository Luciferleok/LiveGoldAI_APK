package com.example.livegoldai.model

import kotlinx.serialization.Serializable

@Serializable
enum class Signal(val label: String) {
    BUY("BUY"),
    SELL("SELL"),
    WAIT("WAIT")
}

@Serializable
data class CandleBar(
    val datetime: String,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val ema9: Double? = null,
    val ema21: Double? = null,
    val bbUpper: Double? = null,
    val bbLower: Double? = null,
    val volume: Double? = null
)

@Serializable
data class IndicatorItem(
    val name: String,
    val signal: Signal,
    val valueDisplay: String,
    val detail: String
)

@Serializable
data class GroupAnalysis(
    val key: String,
    val title: String,
    val verdict: Signal,
    val indicators: List<IndicatorItem>
)

@Serializable
data class PivotLevels(
    val pivot: Double,
    val r1: Double,
    val r2: Double,
    val s1: Double,
    val s2: Double
)

@Serializable
data class TradeSetup(
    val signal: Signal,
    val entryPrice: Double,
    val stopLoss: Double,
    val stopLossPips: Double,
    val takeProfit1: Double,
    val takeProfit1Pips: Double,
    val takeProfit2: Double,
    val takeProfit2Pips: Double,
    val riskRewardRatio: String,
    val confidencePercent: Int,
    val strategyNote: String,
    val atrPips: Double
)

@Serializable
data class MarketSession(
    val name: String,
    val city: String,
    val timeWindowUtc: String,
    val isOpen: Boolean,
    val volatilityLevel: String,
    val isGoldenOverlap: Boolean = false
)

@Serializable
data class GoldAnalysisResult(
    val symbol: String = "XAU/USD",
    val currentPrice: Double,
    val prevClose: Double,
    val changeAmount: Double,
    val changePercent: Double,
    val high24h: Double,
    val low24h: Double,
    val interval: String,
    val lastUpdated: String,
    val overallSignal: Signal,
    val agreementPercent: Double,
    val buyCount: Int,
    val sellCount: Int,
    val waitCount: Int,
    val totalGroups: Int = 5,
    val groups: List<GroupAnalysis>,
    val pivotLevels: PivotLevels,
    val tradeSetup: TradeSetup,
    val marketSessions: List<MarketSession> = emptyList(),
    val recentCandles: List<CandleBar> = emptyList(),
    val isSimulatedFallback: Boolean = false
)

