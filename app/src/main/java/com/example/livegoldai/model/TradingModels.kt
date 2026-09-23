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
    val superTrend: Double? = null,
    val volume: Double? = null,
    val vwap: Double? = null,
    val buyVolume: Double? = null
)

@Serializable
data class BuyerSellerSentiment(
    val buyersPercent: Int,
    val sellersPercent: Int,
    val buyerVolume: Double,
    val sellerVolume: Double,
    val netVolumeDelta: Double,
    val orderBookBidCount: Int,
    val orderBookAskCount: Int,
    val retailSentimentBias: Signal,
    val institutionalSentimentBias: Signal,
    val liveActionHindi: String,
    val liveActionEnglish: String,
    val strengthLevel: String
)

@Serializable
data class EconomicEvent(
    val title: String,
    val country: String = "USD",
    val date: String,
    val time: String,
    val impact: String, // "High", "Medium", "Low", "Holiday"
    val forecast: String = "",
    val previous: String = "",
    val goldImpact: String = "Volatile Reaction",
    val isoTime: String = ""
)

@Serializable
data class MacroMarketIndex(
    val symbol: String,
    val name: String,
    val value: Double,
    val changePercent: Double,
    val impactOnGold: Signal,
    val explanation: String
)

@Serializable
data class NewsSentimentItem(
    val headline: String,
    val source: String,
    val timestamp: String,
    val sentiment: Signal,
    val impactTag: String,
    val reason: String
)

@Serializable
data class MacroSentimentRadar(
    val overallBias: Signal,
    val sentimentScorePercent: Int, // e.g. 75% Bullish Gold
    val dxyIndex: MacroMarketIndex,
    val us10yYield: MacroMarketIndex,
    val upcomingEvents: List<EconomicEvent> = emptyList(),
    val newsFeed: List<NewsSentimentItem> = emptyList(),
    val summaryInsight: String
)

@Serializable
data class SmartMoneyAnalysis(
    val superTrendSignal: Signal,
    val superTrendValue: Double,
    val vwapValue: Double,
    val vwapSignal: Signal,
    val mfi14: Double,
    val mfiSignal: Signal,
    val fib0618: Double,
    val fib0500: Double,
    val marketStructure: String, // "BOS Bullish", "CHoCH Reversal", "Range Compression"
    val liquiditySweepAlert: String
)

@Serializable
data class AccountTierRisk(
    val balanceLabel: String,
    val safeLotSize: String,
    val riskAmountDollars: String,
    val rewardTp1Dollars: String,
    val rewardTp2Dollars: String
)

@Serializable
data class NextPredictionPlaybook(
    val verdict: Signal,
    val urgencyTag: String, // "STRONG CONVICTION", "ACCUMULATE ON DIP", "WAIT / NO TRADE"
    val winProbabilityPercent: Int,
    val actionHeading: String,
    val tradeType: String = "INTRADAY SCALP / SWING", // "BUY SCALP (M15)", "SWING BUY (H1/H4)", "WAIT"
    val orderExecutionType: String = "BUY LIMIT / PULLBACK", // "BUY LIMIT @ $X", "MARKET EXECUTION"
    val recommendedEntryZone: String,
    val stopLossLevel: String,
    val stopLossPips: Double = 18.0,
    val stopLossRationale: String = "Placed strictly below swing structure & SuperTrend trail to prevent stop hunts",
    val takeProfit1: String,
    val takeProfit2: String,
    val takeProfit3: String = "",
    val whereToEnterHindi: String = "",
    val whereToAvoidHindi: String = "",
    val whatToDoHindi: String,
    val whatToDoEnglish: String,
    val hindiAudioAdvice: String = "",
    val executionRules: List<String> = emptyList(),
    val accountTierMatrix: List<AccountTierRisk> = emptyList(),
    val profitProjection001Lot: String,
    val profitProjection010Lot: String,
    val profitProjection100Lot: String,
    val timeHorizon: String,
    val riskManagementRule: String
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
data class CandleReadingInsight(
    val lastCandleType: String, // e.g. "Bullish Hammer / Liquidity Wick"
    val lastCandleMeaning: String, // "Strong buyer absorption at support"
    val upperWickPressure: String, // "Low (No seller resistance)"
    val lowerWickRejection: String, // "High ($4.80 liquidity absorption)"
    val bodyMomentum: String, // "Bullish Expansion (74% body ratio)"
    val nextCandleForecast: String, // "High probability of Green Bullish Expansion candle"
    val nextCandleExpectedRange: String, // "$4,382.00 - $4,396.00"
    val nextCandleTradeTactic: String, // "Buy the initial lower dip wick within first 2 minutes of the candle"
    val confidencePercent: Int // 88%
)

@Serializable
data class TimeframeStatus(
    val timeframe: String, // "1M", "5M", "15M", "1H", "4H", "1D"
    val label: String, // "Micro Scalp", "Fast Scalp", "Intraday Primary", "Hourly Trend", "Institutional Swing", "Daily Macro"
    val signal: Signal, // BUY, SELL, WAIT
    val keyLevel: String, // "$4,381.20 Support", "$4,395.00 Target"
    val momentumPercent: Int, // 85%
    val quickAction: String // "Scalp Long on dip", "Ride Bullish Expansion", "Hold"
)

@Serializable
data class MultiTimeframeMatrix(
    val timeframes: List<TimeframeStatus>,
    val alignmentSummary: String, // "5 of 6 Timeframes BULLISH • MAXIMUM CONFLUENCE"
    val scalpRecommendation: String, // "1M & 5M: Immediate Buy on Dip targeting +12 to +25 pips"
    val swingRecommendation: String // "15M to 4H: Strong Bullish Hold targeting +60 to +140 pips"
)

@Serializable
data class TradingTrick(
    val id: String,
    val name: String, // e.g. "Wick Trap & Liquidity Grab Trick", "Fair Value Gap (FVG) Magnet Trick"
    val winRate: String, // "89% Win Rate"
    val status: String, // "ACTIVE TRIGGER 🟢", "WATCHING 🟡", "READY"
    val triggerCondition: String, // "Price wicked below S1 then instantly reclaimed above VWAP"
    val howToTradeHindi: String, // "Jaise hi fake breakdown ke baad price wapas candle close upar kare, turant BUY karein!"
    val howToTradeEnglish: String, // "Enter long immediately once candle closes back inside range after liquidity sweep."
    val expectedPipGain: String // "+20 to +45 Pips"
)

@Serializable
enum class PredictionOutcomeStatus {
    TP1_HIT,
    TP2_HIT,
    ALL_TARGETS_HIT,
    STOP_LOSS_HIT,
    IN_PROFIT_ACTIVE,
    PENDING_ENTRY
}

@Serializable
data class PastPredictionAuditItem(
    val id: String,
    val timestamp: String,
    val timeAgo: String,
    val signal: Signal,
    val entryPrice: Double,
    val target1Price: Double,
    val target2Price: Double,
    val stopLossPrice: Double,
    val actualHighLowReached: Double,
    val pipsResult: Double,
    val outcomeStatus: PredictionOutcomeStatus,
    val whyItHappenedHindi: String,
    val whyItHappenedEnglish: String,
    val lessonLearnedHindi: String,
    val lessonLearnedEnglish: String,
    val indicatorsInvolved: List<String>
)

@Serializable
data class TimeframeAccuracyAudit(
    val timeframe: String,
    val totalSignalsTested: Int,
    val winCount: Int,
    val lossCount: Int,
    val activeCount: Int,
    val winRatePercent: Int,
    val netPipsGained: Double,
    val lastPredictionOutcome: PastPredictionAuditItem?,
    val recentSignalAudits: List<PastPredictionAuditItem>,
    val autoCorrectionRules: List<String>,
    val autoCorrectionRulesHindi: List<String>,
    val aiEngineLearningStatus: String
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
    val totalGroups: Int = 7,
    val groups: List<GroupAnalysis>,
    val pivotLevels: PivotLevels,
    val tradeSetup: TradeSetup,
    val marketSessions: List<MarketSession> = emptyList(),
    val recentCandles: List<CandleBar> = emptyList(),
    val nextPrediction: NextPredictionPlaybook? = null,
    val macroRadar: MacroSentimentRadar? = null,
    val smartMoney: SmartMoneyAnalysis? = null,
    val candleInsight: CandleReadingInsight? = null,
    val mtfMatrix: MultiTimeframeMatrix? = null,
    val tradingTricks: List<TradingTrick> = emptyList(),
    val buyerSellerRatio: BuyerSellerSentiment? = null,
    val timeframeAudit: TimeframeAccuracyAudit? = null,
    val isSimulatedFallback: Boolean = false,
    val newsMode: NewsModeStatus? = null
)

@Serializable
data class NewsModeStatus(
    val phase: String, // "PRE" = release aane wali hai, "POST" = release ho chuki
    val eventTitle: String,
    val minutes: Long,
    val newsSignal: Signal,
    val technicalSignal: Signal,
    val headline: String,
    val detail: String
)
