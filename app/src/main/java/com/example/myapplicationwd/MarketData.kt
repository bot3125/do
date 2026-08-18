package com.example.myapplicationwd

import com.google.gson.annotations.SerializedName

// Estructuras de datos para la respuesta de Yahoo Finance (Dólar / Soles)
data class YahooResponse(
    @SerializedName("chart") val chart: YahooChart
)

data class YahooChart(
    @SerializedName("result") val result: List<YahooResult>?
)

data class YahooResult(
    @SerializedName("indicators") val indicators: YahooIndicators
)

data class YahooIndicators(
    @SerializedName("quote") val quote: List<YahooQuote>
)

data class YahooQuote(
    @SerializedName("low") val low: List<Double?>,
    @SerializedName("high") val high: List<Double?>
)