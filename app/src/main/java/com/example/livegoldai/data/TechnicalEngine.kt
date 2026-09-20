package com.example.livegoldai.data

import com.example.livegoldai.model.*
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object TechnicalEngine {

    fun analyze(candles: List<CandleBar>, interval: String = "4h"): GoldAnalysisResult {
        if (candles.size < 30) {
            return fallbackAnalysis(interval)
        }

        val n = candles.size
        val closes = candles.map { it.close }
        val highs = candles.map { it.high }
        val lows = candles.map { it.low }
        val opens = candles.map { it.open }

        val last = candles[n - 1]
        val prev = candles[n - 2]
        val currentPrice = last.close
        val prevClose = prev.close
        val changeAmount = currentPrice - prevClose
        val changePercent = if (prevClose != 0.0) (changeAmount / prevClose) * 100.0 else 0.0

        val high24h = highs.takeLast(min(24, n)).maxOrNull() ?: last.high
        val low24h = lows.takeLast(min(24, n)).minOrNull() ?: last.low

        // EMAs
        val ema9 = calculateEma(closes, 9)
        val ema21 = calculateEma(closes, 21)
        val ema50 = calculateEma(closes, 50)
        val ema12 = calculateEma(closes, 12)
        val ema26 = calculateEma(closes, 26)

        // MACD
        val macdLine = ema12.zip(ema26) { e12, e26 -> e12 - e26 }
        val macdSignal = calculateEma(macdLine, 9)

        val lastEma9 = ema9.last()
        val lastEma21 = ema21.last()
        val lastEma50 = ema50.last()
        val lastMacd = macdLine.last()
        val lastMacdSignal = macdSignal.last()

        // RSI 14
        val rsi14 = calculateRsi(closes, 14).last()

        // ATR 14
        val trList = mutableListOf<Double>()
        for (i in 0 until n) {
            if (i == 0) {
                trList.add(highs[i] - lows[i])
            } else {
                val tr = maxOf(
                    highs[i] - lows[i],
                    abs(highs[i] - closes[i - 1]),
                    abs(lows[i] - closes[i - 1])
                )
                trList.add(tr)
            }
        }
        val atr14Series = calculateEmaWilder(trList, 14)
        val lastAtr14 = atr14Series.last()

        // ADX & +DI / -DI
        val plusDm = mutableListOf<Double>()
        val minusDm = mutableListOf<Double>()
        for (i in 0 until n) {
            if (i == 0) {
                plusDm.add(0.0)
                minusDm.add(0.0)
            } else {
                val upMove = highs[i] - highs[i - 1]
                val downMove = lows[i - 1] - lows[i]
                if (upMove > downMove && upMove > 0) {
                    plusDm.add(upMove)
                } else {
                    plusDm.add(0.0)
                }
                if (downMove > upMove && downMove > 0) {
                    minusDm.add(downMove)
                } else {
                    minusDm.add(0.0)
                }
            }
        }

        val smoothedPlusDm = calculateEmaWilder(plusDm, 14)
        val smoothedMinusDm = calculateEmaWilder(minusDm, 14)

        val plusDi = if (lastAtr14 != 0.0) (smoothedPlusDm.last() / lastAtr14) * 100.0 else 0.0
        val minusDi = if (lastAtr14 != 0.0) (smoothedMinusDm.last() / lastAtr14) * 100.0 else 0.0
        val dx = if (plusDi + minusDi != 0.0) (abs(plusDi - minusDi) / (plusDi + minusDi)) * 100.0 else 0.0
        val lastAdx = dx

        // Parabolic SAR approximation
        val minLow5 = lows.takeLast(5).minOrNull() ?: last.low
        val sarBull = last.close > minLow5

        // Stochastic %K
        val low14 = lows.takeLast(14).minOrNull() ?: last.low
        val high14 = highs.takeLast(14).maxOrNull() ?: last.high
        val stochK = if (high14 != low14) ((last.close - low14) / (high14 - low14)) * 100.0 else 50.0

        // CCI 20
        val tpList = (0 until n).map { (highs[it] + lows[it] + closes[it]) / 3.0 }
        val last20Tp = tpList.takeLast(20)
        val smaTp = last20Tp.average()
        val mad = last20Tp.map { abs(it - smaTp) }.average()
        val lastCci = if (mad != 0.0) (tpList.last() - smaTp) / (0.015 * mad) else 0.0

        // Williams %R
        val williamsR = if (high14 != low14) -100.0 * ((high14 - last.close) / (high14 - low14)) else -50.0

        // ROC 10
        val close10Ago = if (n >= 11) closes[n - 11] else closes[0]
        val roc = if (close10Ago != 0.0) ((last.close - close10Ago) / close10Ago) * 100.0 else 0.0

        // Bollinger Bands 20
        val last20Closes = closes.takeLast(20)
        val bbMid = last20Closes.average()
        val variance = last20Closes.map { (it - bbMid) * (it - bbMid) }.average()
        val std20 = sqrt(variance)
        val bbUpper = bbMid + (2 * std20)
        val bbLower = bbMid - (2 * std20)

        // Keltner Channel
        val kcUpper = lastEma21 + (2 * lastAtr14)
        val kcLower = lastEma21 - (2 * lastAtr14)

        // Pivot Points (Classic Floor)
        val pivot = (prev.high + prev.low + prev.close) / 3.0
        val r1 = (2 * pivot) - prev.low
        val s1 = (2 * pivot) - prev.high
        val r2 = pivot + (prev.high - prev.low)
        val s2 = pivot - (prev.high - prev.low)
        val pivotLevels = PivotLevels(
            pivot = pivot,
            r1 = r1,
            r2 = r2,
            s1 = s1,
            s2 = s2
        )

        // Candlestick Patterns
        val body = abs(last.close - last.open)
        val upperWick = last.high - max(last.close, last.open)
        val lowerWick = min(last.close, last.open) - last.low
        val candleRange = last.high - last.low

        val engulfSignal = if (prev.close < prev.open && last.close > last.open && last.close > prev.open && last.open < prev.close) {
            Signal.BUY
        } else if (prev.close > prev.open && last.close < last.open && last.open > prev.close && last.close < prev.open) {
            Signal.SELL
        } else {
            Signal.WAIT
        }

        val wickSignal = if (lowerWick > 2 * body && upperWick < body) {
            Signal.BUY // Hammer
        } else if (upperWick > 2 * body && lowerWick < body) {
            Signal.SELL // Shooting star
        } else {
            Signal.WAIT
        }

        val candleDirSignal = if (candleRange > 0 && body < candleRange * 0.1) {
            Signal.WAIT // Doji
        } else if (last.close > last.open) {
            Signal.BUY
        } else {
            Signal.SELL
        }

        // --- Group 1: Trend ---
        val trendItems = listOf(
            IndicatorItem(
                name = "EMA 9 vs EMA 21",
                signal = if (lastEma9 > lastEma21) Signal.BUY else Signal.SELL,
                valueDisplay = "EMA9: ${format2(lastEma9)} | EMA21: ${format2(lastEma21)}",
                detail = if (lastEma9 > lastEma21) "Fast EMA above slow (Bullish Crossover)" else "Fast EMA below slow (Bearish Crossover)"
            ),
            IndicatorItem(
                name = "Close vs EMA 50",
                signal = if (last.close > lastEma50) Signal.BUY else Signal.SELL,
                valueDisplay = "Price: ${format2(last.close)} | EMA50: ${format2(lastEma50)}",
                detail = if (last.close > lastEma50) "Trading above long-term trend baseline" else "Trading below long-term trend baseline"
            ),
            IndicatorItem(
                name = "MACD vs Signal",
                signal = if (lastMacd > lastMacdSignal) Signal.BUY else Signal.SELL,
                valueDisplay = "MACD: ${format2(lastMacd)} | Sig: ${format2(lastMacdSignal)}",
                detail = if (lastMacd > lastMacdSignal) "MACD line above signal line (Upward drive)" else "MACD line below signal line (Downward drive)"
            ),
            IndicatorItem(
                name = "ADX & DI Strength",
                signal = if (lastAdx > 20) (if (plusDi > minusDi) Signal.BUY else Signal.SELL) else Signal.WAIT,
                valueDisplay = "ADX: ${format1(lastAdx)} | +DI: ${format1(plusDi)} -DI: ${format1(minusDi)}",
                detail = if (lastAdx > 20) (if (plusDi > minusDi) "Strong Bullish Trend (+DI dominant)" else "Strong Bearish Trend (-DI dominant)") else "Weak or consolidating trend (ADX < 20)"
            ),
            IndicatorItem(
                name = "Parabolic SAR",
                signal = if (sarBull) Signal.BUY else Signal.SELL,
                valueDisplay = if (sarBull) "BULLISH (Above 5-low)" else "BEARISH (Below 5-low)",
                detail = if (sarBull) "Price holding above recent swing-low support" else "Price breached recent swing-low"
            )
        )
        val trendVerdict = decide(trendItems.map { it.signal })

        // --- Group 2: Momentum ---
        val momentumItems = listOf(
            IndicatorItem(
                name = "RSI (14)",
                signal = if (rsi14 >= 55) Signal.BUY else if (rsi14 <= 45) Signal.SELL else Signal.WAIT,
                valueDisplay = "${format1(rsi14)} / 100",
                detail = when {
                    rsi14 >= 70 -> "Overbought zone with strong momentum"
                    rsi14 >= 55 -> "Bullish momentum zone (>55)"
                    rsi14 <= 30 -> "Oversold territory"
                    rsi14 <= 45 -> "Bearish momentum zone (<45)"
                    else -> "Neutral equilibrium (45-55)"
                }
            ),
            IndicatorItem(
                name = "Stochastic %K",
                signal = if (stochK < 20) Signal.BUY else if (stochK > 80) Signal.SELL else Signal.WAIT,
                valueDisplay = "${format1(stochK)}%",
                detail = when {
                    stochK < 20 -> "Oversold bounce potential (<20)"
                    stochK > 80 -> "Overbought exhaustion alert (>80)"
                    else -> "Mid-range zone (20-80)"
                }
            ),
            IndicatorItem(
                name = "CCI (20)",
                signal = if (lastCci < -100) Signal.BUY else if (lastCci > 100) Signal.SELL else Signal.WAIT,
                valueDisplay = format1(lastCci),
                detail = when {
                    lastCci < -100 -> "Extreme cyclical oversold dip (BUY)"
                    lastCci > 100 -> "Extreme cyclical overbought peak (SELL)"
                    else -> "Normal oscillation range (-100 to +100)"
                }
            ),
            IndicatorItem(
                name = "Williams %R",
                signal = if (williamsR < -80) Signal.BUY else if (williamsR > -20) Signal.SELL else Signal.WAIT,
                valueDisplay = "${format1(williamsR)}%",
                detail = when {
                    williamsR < -80 -> "Oversold zone (< -80)"
                    williamsR > -20 -> "Overbought zone (> -20)"
                    else -> "Neutral band (-80 to -20)"
                }
            ),
            IndicatorItem(
                name = "ROC (10) Velocity",
                signal = if (roc > 0) Signal.BUY else Signal.SELL,
                valueDisplay = "${if (roc > 0) "+" else ""}${format2(roc)}%",
                detail = if (roc > 0) "Price momentum positive over 10 bars" else "Price momentum negative over 10 bars"
            )
        )
        val momentumVerdict = decide(momentumItems.map { it.signal })

        // --- Group 3: Volatility ---
        val volatilityItems = listOf(
            IndicatorItem(
                name = "Bollinger Bands",
                signal = if (last.close < bbLower) Signal.BUY else if (last.close > bbUpper) Signal.SELL else Signal.WAIT,
                valueDisplay = "U: ${format2(bbUpper)} | L: ${format2(bbLower)}",
                detail = when {
                    last.close < bbLower -> "Pierced lower volatility band (Oversold squeeze)"
                    last.close > bbUpper -> "Pierced upper volatility band (Overbought stretch)"
                    else -> "Within 2-sigma volatility envelope"
                }
            ),
            IndicatorItem(
                name = "Keltner Channel",
                signal = if (last.close < kcLower) Signal.BUY else if (last.close > kcUpper) Signal.SELL else Signal.WAIT,
                valueDisplay = "U: ${format2(kcUpper)} | L: ${format2(kcLower)}",
                detail = when {
                    last.close < kcLower -> "Below ATR lower channel boundary"
                    last.close > kcUpper -> "Above ATR upper channel boundary"
                    else -> "Inside ATR volatility channel"
                }
            ),
            IndicatorItem(
                name = "Std Dev Bands",
                signal = if (last.close < (bbMid - 2 * std20)) Signal.BUY else if (last.close > (bbMid + 2 * std20)) Signal.SELL else Signal.WAIT,
                valueDisplay = "Std: ${format2(std20)} | Mid: ${format2(bbMid)}",
                detail = "2-sigma statistical dispersion threshold"
            )
        )
        val volatilityVerdict = decide(volatilityItems.map { it.signal })

        // --- Group 4: Support & Resistance ---
        val srItems = listOf(
            IndicatorItem(
                name = "Floor Pivot (P)",
                signal = if (last.close > pivot) Signal.BUY else Signal.SELL,
                valueDisplay = "P: $${format2(pivot)}",
                detail = if (last.close > pivot) "Bullish stance above daily central pivot" else "Bearish stance below daily central pivot"
            ),
            IndicatorItem(
                name = "R1 / S1 Boundary Levels",
                signal = if (last.close < s1) Signal.BUY else if (last.close > r1) Signal.SELL else Signal.WAIT,
                valueDisplay = "R1: $${format2(r1)} | S1: $${format2(s1)}",
                detail = when {
                    last.close < s1 -> "Below S1 support (Rebound demand zone)"
                    last.close > r1 -> "Above R1 resistance (Supply barrier zone)"
                    else -> "Operating inside S1 to R1 channel"
                }
            )
        )
        val srVerdict = decide(srItems.map { it.signal })

        // --- Group 5: Candlestick Patterns ---
        val candleItems = listOf(
            IndicatorItem(
                name = "Engulfing Pattern",
                signal = engulfSignal,
                valueDisplay = when (engulfSignal) {
                    Signal.BUY -> "Bullish Engulfing"
                    Signal.SELL -> "Bearish Engulfing"
                    Signal.WAIT -> "No Engulfing detected"
                },
                detail = when (engulfSignal) {
                    Signal.BUY -> "Current green body fully consumed previous red candle"
                    Signal.SELL -> "Current red body fully consumed previous green candle"
                    Signal.WAIT -> "Standard candle structure"
                }
            ),
            IndicatorItem(
                name = "Hammer / Shooting Star",
                signal = wickSignal,
                valueDisplay = when (wickSignal) {
                    Signal.BUY -> "Bullish Hammer"
                    Signal.SELL -> "Shooting Star"
                    Signal.WAIT -> "Balanced wicks"
                },
                detail = when (wickSignal) {
                    Signal.BUY -> "Long lower wick showing heavy buyer rejection of lower prices"
                    Signal.SELL -> "Long upper wick showing strong seller rejection of higher prices"
                    Signal.WAIT -> "No extreme wick rejection"
                }
            ),
            IndicatorItem(
                name = "Candle Direction",
                signal = candleDirSignal,
                valueDisplay = when (candleDirSignal) {
                    Signal.BUY -> "Bullish Green (${format2(last.open)} -> ${format2(last.close)})"
                    Signal.SELL -> "Bearish Red (${format2(last.open)} -> ${format2(last.close)})"
                    Signal.WAIT -> "Doji / Neutral (${format2(last.close)})"
                },
                detail = when (candleDirSignal) {
                    Signal.BUY -> "Buyers firmly in control of bar close"
                    Signal.SELL -> "Sellers drove close beneath the open"
                    Signal.WAIT -> "Indecision doji bar"
                }
            )
        )
        val candleVerdict = decide(candleItems.map { it.signal })

        // Group Summaries
        val groups = listOf(
            GroupAnalysis(key = "trend", title = "Trend Strength", verdict = trendVerdict, indicators = trendItems),
            GroupAnalysis(key = "momentum", title = "Momentum Oscillators", verdict = momentumVerdict, indicators = momentumItems),
            GroupAnalysis(key = "volatility", title = "Volatility Bands", verdict = volatilityVerdict, indicators = volatilityItems),
            GroupAnalysis(key = "sr", title = "Support & Resistance", verdict = srVerdict, indicators = srItems),
            GroupAnalysis(key = "candlestick", title = "Candlestick Action", verdict = candleVerdict, indicators = candleItems)
        )

        val buyCount = groups.count { it.verdict == Signal.BUY }
        val sellCount = groups.count { it.verdict == Signal.SELL }
        val waitCount = groups.count { it.verdict == Signal.WAIT }
        val total = groups.size

        val (overallSignal, agreementPercent) = when {
            buyCount > sellCount -> Pair(Signal.BUY, (buyCount.toDouble() / total) * 100.0)
            sellCount > buyCount -> Pair(Signal.SELL, (sellCount.toDouble() / total) * 100.0)
            else -> Pair(Signal.WAIT, 0.0)
        }

        // Enrich candles with EMA and Bollinger bands for chart overlays
        val enrichedCandles = candles.takeLast(60).mapIndexed { idx, bar ->
            val globalIdx = (candles.size - min(60, candles.size)) + idx
            val cEma9 = if (globalIdx < ema9.size) ema9[globalIdx] else null
            val cEma21 = if (globalIdx < ema21.size) ema21[globalIdx] else null
            val cBbUpper = if (globalIdx >= 19) {
                val window = closes.subList(max(0, globalIdx - 19), globalIdx + 1)
                val m = window.average()
                val sd = sqrt(window.map { (it - m) * (it - m) }.average())
                m + 2 * sd
            } else null
            val cBbLower = if (globalIdx >= 19) {
                val window = closes.subList(max(0, globalIdx - 19), globalIdx + 1)
                val m = window.average()
                val sd = sqrt(window.map { (it - m) * (it - m) }.average())
                m - 2 * sd
            } else null
            val estVolume = (abs(bar.close - bar.open) + (bar.high - bar.low)) * 1420.0 + 800.0

            bar.copy(
                ema9 = cEma9,
                ema21 = cEma21,
                bbUpper = cBbUpper,
                bbLower = cBbLower,
                volume = estVolume
            )
        }

        // Calculate actionable Trade Setup
        val atrSafe = if (lastAtr14 > 0.5) lastAtr14 else 6.5
        val tradeSetup = when (overallSignal) {
            Signal.BUY -> {
                val sl = currentPrice - (1.5 * atrSafe)
                val tp1 = currentPrice + (1.5 * atrSafe)
                val tp2 = currentPrice + (3.0 * atrSafe)
                TradeSetup(
                    signal = Signal.BUY,
                    entryPrice = currentPrice,
                    stopLoss = sl,
                    stopLossPips = (currentPrice - sl) * 10.0,
                    takeProfit1 = tp1,
                    takeProfit1Pips = (tp1 - currentPrice) * 10.0,
                    takeProfit2 = tp2,
                    takeProfit2Pips = (tp2 - currentPrice) * 10.0,
                    riskRewardRatio = "1:2.0",
                    confidencePercent = max(60, agreementPercent.toInt()),
                    strategyNote = "Bullish momentum aligned across indicators. Enter near ${format2(currentPrice)}, target R1/R2 with SL under pivot support.",
                    atrPips = atrSafe * 10.0
                )
            }
            Signal.SELL -> {
                val sl = currentPrice + (1.5 * atrSafe)
                val tp1 = currentPrice - (1.5 * atrSafe)
                val tp2 = currentPrice - (3.0 * atrSafe)
                TradeSetup(
                    signal = Signal.SELL,
                    entryPrice = currentPrice,
                    stopLoss = sl,
                    stopLossPips = (sl - currentPrice) * 10.0,
                    takeProfit1 = tp1,
                    takeProfit1Pips = (currentPrice - tp1) * 10.0,
                    takeProfit2 = tp2,
                    takeProfit2Pips = (currentPrice - tp2) * 10.0,
                    riskRewardRatio = "1:2.0",
                    confidencePercent = max(60, agreementPercent.toInt()),
                    strategyNote = "Bearish rejection confirmed. Enter near ${format2(currentPrice)}, target S1/S2 with tight stop above resistance.",
                    atrPips = atrSafe * 10.0
                )
            }
            Signal.WAIT -> {
                val sl = currentPrice - (1.2 * atrSafe)
                val tp1 = currentPrice + (1.2 * atrSafe)
                val tp2 = currentPrice + (2.4 * atrSafe)
                TradeSetup(
                    signal = Signal.WAIT,
                    entryPrice = currentPrice,
                    stopLoss = sl,
                    stopLossPips = (currentPrice - sl) * 10.0,
                    takeProfit1 = tp1,
                    takeProfit1Pips = (tp1 - currentPrice) * 10.0,
                    takeProfit2 = tp2,
                    takeProfit2Pips = (tp2 - currentPrice) * 10.0,
                    riskRewardRatio = "1:1.5",
                    confidencePercent = 50,
                    strategyNote = "Range compression detected. Stand by for clear breakout above R1 (${format2(r1)}) or breakdown below S1 (${format2(s1)}).",
                    atrPips = atrSafe * 10.0
                )
            }
        }

        val marketSessions = calculateMarketSessions()

        return GoldAnalysisResult(
            symbol = "XAU/USD",
            currentPrice = currentPrice,
            prevClose = prevClose,
            changeAmount = changeAmount,
            changePercent = changePercent,
            high24h = high24h,
            low24h = low24h,
            interval = interval,
            lastUpdated = last.datetime,
            overallSignal = overallSignal,
            agreementPercent = agreementPercent,
            buyCount = buyCount,
            sellCount = sellCount,
            waitCount = waitCount,
            totalGroups = total,
            groups = groups,
            pivotLevels = pivotLevels,
            tradeSetup = tradeSetup,
            marketSessions = marketSessions,
            recentCandles = enrichedCandles,
            isSimulatedFallback = false
        )
    }

    fun calculateMarketSessions(): List<MarketSession> {
        val nowUtc = java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC)
        val utcHour = nowUtc.hour
        val isGoldenOverlap = utcHour in 13..16

        return listOf(
            MarketSession(
                name = "London",
                city = "London (LSE)",
                timeWindowUtc = "08:00 - 17:00 UTC",
                isOpen = utcHour in 8..16,
                volatilityLevel = if (isGoldenOverlap) "Extreme (Overlap)" else "High",
                isGoldenOverlap = isGoldenOverlap
            ),
            MarketSession(
                name = "New York",
                city = "New York (COMEX)",
                timeWindowUtc = "13:00 - 22:00 UTC",
                isOpen = utcHour in 13..21,
                volatilityLevel = if (isGoldenOverlap) "Extreme (Overlap)" else "High",
                isGoldenOverlap = isGoldenOverlap
            ),
            MarketSession(
                name = "Tokyo",
                city = "Tokyo (Tocom)",
                timeWindowUtc = "00:00 - 09:00 UTC",
                isOpen = utcHour in 0..8,
                volatilityLevel = "Moderate",
                isGoldenOverlap = false
            ),
            MarketSession(
                name = "Sydney",
                city = "Sydney (ASX)",
                timeWindowUtc = "22:00 - 07:00 UTC",
                isOpen = utcHour >= 22 || utcHour < 7,
                volatilityLevel = "Normal",
                isGoldenOverlap = false
            )
        )
    }

    private fun decide(signals: List<Signal>): Signal {
        val buy = signals.count { it == Signal.BUY }
        val sell = signals.count { it == Signal.SELL }
        return when {
            buy > sell -> Signal.BUY
            sell > buy -> Signal.SELL
            else -> Signal.WAIT
        }
    }

    private fun calculateEma(values: List<Double>, span: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val alpha = 2.0 / (span + 1.0)
        val result = ArrayList<Double>(values.size)
        var currentEma = values[0]
        result.add(currentEma)
        for (i in 1 until values.size) {
            currentEma = alpha * values[i] + (1.0 - alpha) * currentEma
            result.add(currentEma)
        }
        return result
    }

    private fun calculateEmaWilder(values: List<Double>, span: Int): List<Double> {
        if (values.isEmpty()) return emptyList()
        val alpha = 1.0 / span.toDouble()
        val result = ArrayList<Double>(values.size)
        var currentEma = values[0]
        result.add(currentEma)
        for (i in 1 until values.size) {
            currentEma = alpha * values[i] + (1.0 - alpha) * currentEma
            result.add(currentEma)
        }
        return result
    }

    private fun calculateRsi(closes: List<Double>, period: Int = 14): List<Double> {
        if (closes.size <= period) return List(closes.size) { 50.0 }
        val gains = mutableListOf<Double>()
        val losses = mutableListOf<Double>()
        for (i in 1 until closes.size) {
            val delta = closes[i] - closes[i - 1]
            if (delta >= 0) {
                gains.add(delta)
                losses.add(0.0)
            } else {
                gains.add(0.0)
                losses.add(abs(delta))
            }
        }
        val alpha = 1.0 / period.toDouble()
        var avgGain = gains.take(period).average()
        var avgLoss = losses.take(period).average()

        val rsiList = mutableListOf<Double>()
        for (i in 0 until period) {
            rsiList.add(50.0)
        }

        for (i in period until gains.size) {
            avgGain = alpha * gains[i] + (1.0 - alpha) * avgGain
            avgLoss = alpha * losses[i] + (1.0 - alpha) * avgLoss
            val rs = if (avgLoss != 0.0) avgGain / avgLoss else 100.0
            val rsi = 100.0 - (100.0 / (1.0 + rs))
            rsiList.add(rsi)
        }
        return rsiList
    }

    private fun format1(v: Double): String = String.format(Locale.US, "%.1f", v)
    private fun format2(v: Double): String = String.format(Locale.US, "%.2f", v)

    fun fallbackAnalysis(interval: String = "4h"): GoldAnalysisResult {
        // High quality realistic Gold data for offline or rate-limited backup
        val basePrice = 2742.60
        val dummyCandles = mutableListOf<CandleBar>()
        var p = basePrice - 15.0
        for (i in 0 until 50) {
            val delta = (Math.sin(i * 0.4) * 3.5) + (if (i > 30) 1.2 else -0.8)
            val open = p
            val close = p + delta
            val high = max(open, close) + abs(Math.cos(i * 0.5) * 2.2)
            val low = min(open, close) - abs(Math.sin(i * 0.7) * 2.0)
            p = close
            dummyCandles.add(
                CandleBar(
                    datetime = "2026-09-16 ${String.format(Locale.US, "%02d:00", (i % 24))}",
                    open = open,
                    high = high,
                    low = low,
                    close = close
                )
            )
        }
        val res = analyze(dummyCandles, interval)
        return res.copy(isSimulatedFallback = true)
    }
}
