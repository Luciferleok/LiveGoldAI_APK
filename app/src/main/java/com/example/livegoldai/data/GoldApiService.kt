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
import java.util.concurrent.TimeUnit

class GoldApiService(
    private var apiKey: String = "8e1493529b8e42d9b0a9e557c3451db0"
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    fun setApiKey(newKey: String) {
        apiKey = newKey.trim()
    }

    fun getApiKey(): String = apiKey

    suspend fun fetchAnalysis(interval: String = "4h", symbol: String = "XAU/USD"): Result<GoldAnalysisResult> = withContext(Dispatchers.IO) {
        val url = "https://api.twelvedata.com/time_series?symbol=$symbol&interval=$interval&outputsize=100&apikey=$apiKey&order=ASC"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "KalankarFXGoldPro/1.0")
            .build()

        try {
            val response = client.newCall(request).execute()
            val bodyString = response.body?.string()

            if (!response.isSuccessful || bodyString.isNullOrBlank()) {
                val fallback = TechnicalEngine.fallbackAnalysis(interval)
                return@withContext Result.success(fallback.copy(isSimulatedFallback = true))
            }

            val jsonElement = json.parseToJsonElement(bodyString).jsonObject
            val valuesArray = jsonElement["values"]?.jsonArray

            if (valuesArray == null || valuesArray.isEmpty()) {
                // Check if API returned an error/rate-limit message
                val message = jsonElement["message"]?.jsonPrimitive?.content ?: "No candle data returned from API."
                val fallback = TechnicalEngine.fallbackAnalysis(interval)
                return@withContext Result.success(fallback.copy(isSimulatedFallback = true))
            }

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

            if (candleList.size < 10) {
                val fallback = TechnicalEngine.fallbackAnalysis(interval)
                return@withContext Result.success(fallback.copy(isSimulatedFallback = true))
            }

            val analysis = TechnicalEngine.analyze(candleList, interval)
            Result.success(analysis)
        } catch (e: Exception) {
            val fallback = TechnicalEngine.fallbackAnalysis(interval)
            Result.success(fallback.copy(isSimulatedFallback = true))
        }
    }
}
