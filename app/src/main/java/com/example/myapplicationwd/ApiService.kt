package com.example.myapplicationwd

import com.google.gson.JsonArray
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// API de Binance para Worldcoin
interface BinanceApi {
    @GET("api/v3/klines")
    suspend fun getHistoricalPrices(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("limit") limit: Int
    ): JsonArray
}

// API de Yahoo Finance para el tipo de cambio USD/PEN
interface YahooApi {
    @GET("v8/finance/chart/{symbol}")
    suspend fun getHistoricalRates(
        @Path("symbol") symbol: String,
        @Query("range") range: String,
        @Query("interval") interval: String
    ): YahooResponse
}