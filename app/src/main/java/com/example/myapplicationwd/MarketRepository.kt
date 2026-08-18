package com.example.myapplicationwd

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MarketRepository {

    private val binanceRetrofit = Retrofit.Builder()
        .baseUrl("https://api.binance.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(BinanceApi::class.java)

    private val yahooRetrofit = Retrofit.Builder()
        .baseUrl("https://query1.finance.yahoo.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(YahooApi::class.java)

    // Calcula el mínimo y máximo de Worldcoin (WLD)
    suspend fun getWldMinMax(): Pair<Double, Double>? {
        return try {
            val response = binanceRetrofit.getHistoricalPrices("WLDUSDT", "1d", 1000)
            var min = Double.MAX_VALUE
            var max = Double.MIN_VALUE

            for (i in 0 until response.size()) {
                val dayData = response[i].asJsonArray
                val high = dayData[2].asString.toDoubleOrNull() ?: continue
                val low = dayData[3].asString.toDoubleOrNull() ?: continue

                if (high > max) max = high
                if (low < min) min = low
            }
            Pair(min, max)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Calcula el mínimo y máximo del Dólar (USD/PEN)
    suspend fun getUsdPenMinMax(): Pair<Double, Double>? {
        return try {
            val response = yahooRetrofit.getHistoricalRates("USDPEN=X", "5y", "1d")
            val quotes = response.chart.result?.getOrNull(0)?.indicators?.quote?.getOrNull(0)

            val lows = quotes?.low?.filterNotNull() ?: emptyList()
            val highs = quotes?.high?.filterNotNull() ?: emptyList()

            val min = lows.minOrNull() ?: return null
            val max = highs.maxOrNull() ?: return null

            Pair(min, max)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}