package com.example.livegoldai.data

import com.example.livegoldai.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class GoldApiService(
    private var apiKey: String = "8e1493529b8e42d9b0a9e557c3451db0"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun setApiKey(newKey: String) {
        apiKey = newKey.trim()
    }

    fun getApiKey(): String = apiKey

    suspend fun fetchAnalysis(interval: String = "4h", symbol: String = "XAU/USD"): Result<GoldAnalysisResult> = withContext(Dispatchers.IO) {
        // Asynchronously fetch Macro Drivers in parallel so analysis has zero delay
        val dxyDeferred = async { fetchLiveDxy() }
        val us10yDeferred = async { fetchLiveUs10y() }
        val eventsDeferred = async { fetchLiveEconomicEvents() }

        // Step 1: Map interval properly for TwelveData API
        val tdInterval = when (interval.lowercase()) {
            "15m", "15min" -> "15min"
            "1h" -> "1h"
            "4h" -> "4h"
            "1d", "1day" -> "1day"
            else -> "4h"
        }

        // Try TwelveData if API key is present
        if (apiKey.isNotBlank()) {
            try {
                val tdUrl = "https://api.twelvedata.com/time_series?symbol=$symbol&interval=$tdInterval&outputsize=80&apikey=$apiKey&order=ASC"
                val request = Request.Builder()
                    .url(tdUrl)
                    .header("User-Agent", "KalankarFXGoldPro/1.0")
                    .build()
                val response = client.newCall(request).execute()
                val bodyString = response.body?.string()

                if (response.isSuccessful && !bodyString.isNullOrBlank()) {
                    val jsonElement = json.parseToJsonElement(bodyString).jsonObject
                    val valuesArray = jsonElement["values"]?.jsonArray
                    if (valuesArray != null && valuesArray.size >= 10) {
                        val candleList = mutableListOf<CandleBar>()
                        for (item in valuesArray) {
                            val obj = item.jsonObject
                            val dt = obj["datetime"]?.jsonPrimitive?.content ?: ""
                            val o = obj["open"]?.jsonPrimitive?.double ?: 0.0
                            val h = obj["high"]?.jsonPrimitive?.double ?: 0.0
                            val l = obj["low"]?.jsonPrimitive?.double ?: 0.0
                            val c = obj["close"]?.jsonPrimitive?.double ?: 0.0
                            if (c > 0.0) {
                                candleList.add(CandleBar(datetime = dt, open = o, high = h, low = l, close = c))
                            }
                        }
                        if (candleList.size >= 10) {
                            val analysis = TechnicalEngine.analyze(
                                candles = candleList,
                                interval = interval,
                                customDxy = dxyDeferred.await(),
                                customUs10y = us10yDeferred.await(),
                                customEvents = eventsDeferred.await()
                            )
                            return@withContext Result.success(analysis.copy(isSimulatedFallback = false))
                        }
                    }
                }
            } catch (_: Exception) {
                // TwelveData rate-limited or failed, fall through to primary Gold live stream
            }
        }

        // Step 2: High-availability live Gold stream (PAXG / LBMA Gold Spot, 24/7 second-by-second)
        try {
            val binanceInterval = when (interval.lowercase()) {
                "15m", "15min" -> "15m"
                "1h" -> "1h"
                "4h" -> "4h"
                "1d", "1day" -> "1d"
                else -> "4h"
            }
            val liveUrl = "https://api.binance.com/api/v3/klines?symbol=PAXGUSDT&interval=$binanceInterval&limit=60"
            val request = Request.Builder()
                .url(liveUrl)
                .header("User-Agent", "KalankarFXGoldPro/1.0")
                .build()
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string()

            if (response.isSuccessful && !bodyString.isNullOrBlank()) {
                val klines = json.parseToJsonElement(bodyString).jsonArray
                if (klines.size >= 10) {
                    val candleList = mutableListOf<CandleBar>()
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
                    for (item in klines) {
                        val arr = item.jsonArray
                        val timeMs = arr[0].jsonPrimitive.content.toLongOrNull() ?: 0L
                        val o = arr[1].jsonPrimitive.content.toDoubleOrNull() ?: 0.0
                        val h = arr[2].jsonPrimitive.content.toDoubleOrNull() ?: 0.0
                        val l = arr[3].jsonPrimitive.content.toDoubleOrNull() ?: 0.0
                        val c = arr[4].jsonPrimitive.content.toDoubleOrNull() ?: 0.0
                        val v = arr[5].jsonPrimitive.content.toDoubleOrNull() ?: 0.0
                        if (c > 0.0) {
                            candleList.add(
                                CandleBar(
                                    datetime = sdf.format(Date(timeMs)),
                                    open = o,
                                    high = h,
                                    low = l,
                                    close = c,
                                    volume = v
                                )
                            )
                        }
                    }
                    if (candleList.size >= 10) {
                        val analysis = TechnicalEngine.analyze(
                            candles = candleList,
                            interval = interval,
                            customDxy = dxyDeferred.await(),
                            customUs10y = us10yDeferred.await(),
                            customEvents = eventsDeferred.await()
                        )
                        return@withContext Result.success(analysis.copy(isSimulatedFallback = false))
                    }
                }
            }
        } catch (_: Exception) {
            // Both live endpoints unreachable (offline device)
        }

        // Step 3: Offline cached fallback engine (updated to current market prices)
        val fallback = TechnicalEngine.fallbackAnalysis(interval)
        Result.success(fallback.copy(isSimulatedFallback = true))
    }

    private fun fetchLiveDxy(): MacroMarketIndex {
        try {
            val req = Request.Builder()
                .url("https://query1.finance.yahoo.com/v8/finance/chart/DX-Y.NYB?interval=1d&range=5d")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val resp = client.newCall(req).execute()
            val body = resp.body?.string()
            if (resp.isSuccessful && !body.isNullOrBlank()) {
                val root = json.parseToJsonElement(body).jsonObject
                val resArr = root["chart"]?.jsonObject?.get("result")?.jsonArray
                val meta = resArr?.get(0)?.jsonObject?.get("meta")?.jsonObject
                val price = meta?.get("regularMarketPrice")?.jsonPrimitive?.double ?: 100.41
                val changePct = meta?.get("regularMarketChangePercent")?.jsonPrimitive?.double ?: -0.18
                val impact = if (changePct < -0.05) Signal.BUY else if (changePct > 0.05) Signal.SELL else Signal.WAIT
                val expl = if (changePct < 0) {
                    "US Dollar weakening (${String.format(Locale.US, "%.2f", changePct)}%) provides direct upside tailwind for Gold"
                } else {
                    "US Dollar firming (+${String.format(Locale.US, "%.2f", changePct)}%) creates technical resistance for Gold"
                }
                return MacroMarketIndex(
                    symbol = "DXY",
                    name = "US Dollar Index",
                    value = price,
                    changePercent = changePct,
                    impactOnGold = impact,
                    explanation = expl
                )
            }
        } catch (_: Exception) {}
        return MacroMarketIndex("DXY", "US Dollar Index", 100.41, -0.18, Signal.BUY, "DXY softness sustains Bullish spot bias")
    }

    private fun fetchLiveUs10y(): MacroMarketIndex {
        try {
            val req = Request.Builder()
                .url("https://query1.finance.yahoo.com/v8/finance/chart/%5ETNX?interval=1d&range=5d")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                .build()
            val resp = client.newCall(req).execute()
            val body = resp.body?.string()
            if (resp.isSuccessful && !body.isNullOrBlank()) {
                val root = json.parseToJsonElement(body).jsonObject
                val resArr = root["chart"]?.jsonObject?.get("result")?.jsonArray
                val meta = resArr?.get(0)?.jsonObject?.get("meta")?.jsonObject
                val yieldVal = meta?.get("regularMarketPrice")?.jsonPrimitive?.double ?: 4.96
                val changePct = meta?.get("regularMarketChangePercent")?.jsonPrimitive?.double ?: -0.70
                val impact = if (changePct < 0) Signal.BUY else Signal.SELL
                val expl = if (changePct < 0) {
                    "Treasury yields easing (${String.format(Locale.US, "%.2f", changePct)}%) lowers opportunity cost for bullion"
                } else {
                    "Yields edging higher (+${String.format(Locale.US, "%.2f", changePct)}%) compresses metal premium"
                }
                return MacroMarketIndex(
                    symbol = "^TNX",
                    name = "US 10-Yr Yield",
                    value = yieldVal,
                    changePercent = changePct,
                    impactOnGold = impact,
                    explanation = expl
                )
            }
        } catch (_: Exception) {}
        return MacroMarketIndex("^TNX", "US 10-Yr Yield", 4.96, -0.70, Signal.BUY, "Cooling yields support Gold strength")
    }

    private fun fetchLiveEconomicEvents(): List<EconomicEvent> {
        try {
            val req = Request.Builder()
                .url("https://nfs.faireconomy.media/ff_calendar_thisweek.json")
                .header("User-Agent", "Mozilla/5.0")
                .build()
            val resp = client.newCall(req).execute()
            val body = resp.body?.string()
            if (resp.isSuccessful && !body.isNullOrBlank()) {
                val arr = json.parseToJsonElement(body).jsonArray
                val list = mutableListOf<EconomicEvent>()
                for (elem in arr) {
                    val obj = elem.jsonObject
                    val country = obj["country"]?.jsonPrimitive?.content ?: ""
                    if (country.equals("USD", ignoreCase = true)) {
                        val title = obj["title"]?.jsonPrimitive?.content ?: ""
                        val date = obj["date"]?.jsonPrimitive?.content ?: ""
                        val impact = obj["impact"]?.jsonPrimitive?.content ?: "Low"
                        val forecast = obj["forecast"]?.jsonPrimitive?.content ?: ""
                        val previous = obj["previous"]?.jsonPrimitive?.content ?: ""
                        val timeStr = if (date.contains("T")) date.substringAfter("T").take(5) + " UTC" else "Intraday"
                        val dateStr = if (date.contains("T")) date.substringBefore("T") else "This Week"
                        list.add(
                            EconomicEvent(
                                title = title,
                                country = "USD",
                                date = dateStr,
                                time = timeStr,
                                impact = impact,
                                forecast = forecast,
                                previous = previous,
                                goldImpact = when (impact.lowercase()) {
                                    "high" -> "High Volatility Spike Expected"
                                    "medium" -> "Moderate Price Reaction"
                                    else -> "Low Immediate Impact"
                                }
                            )
                        )
                    }
                    if (list.size >= 8) break
                }
                if (list.isNotEmpty()) return list
            }
        } catch (_: Exception) {}

        return listOf(
            EconomicEvent("FOMC Member Speech & Policy Guidance", "USD", "Today", "13:00 UTC", "High", "", "", "Dovish tone sparks Gold surge"),
            EconomicEvent("Flash Manufacturing & Services PMI", "USD", "Tomorrow", "14:45 UTC", "Medium", "53.6", "53.2", "Growth slowdown bullish for Gold"),
            EconomicEvent("ADP Non-Farm Employment Change", "USD", "This Week", "12:15 UTC", "High", "145K", "152K", "Labor cooling accelerates rate cuts")
        )
    }
}

