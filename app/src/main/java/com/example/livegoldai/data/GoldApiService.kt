package com.example.livegoldai.data

import com.example.livegoldai.model.CandleBar
import com.example.livegoldai.model.GoldAnalysisResult
import kotlinx.coroutines.Dispatchers
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

class GoldApiService(
    private var apiKey: String = "8e1493529b8e42d9b0a9e557c3451db0"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun setApiKey(newKey: String) {
        apiKey = newKey.trim()
    }

    fun getApiKey(): String = apiKey

    suspend fun fetchAnalysis(interval: String = "4h", symbol: String = "XAU/USD"): Result<GoldAnalysisResult> = withContext(Dispatchers.IO) {
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
                            val analysis = TechnicalEngine.analyze(candleList, interval)
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
                        val analysis = TechnicalEngine.analyze(candleList, interval)
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
}

