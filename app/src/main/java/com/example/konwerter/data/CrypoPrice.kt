package com.example.konwerter.data

data class CrypoPrice(
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class CoinGeckoResponse(
    val bitcoin: CryptoData?,
    val ethereum: CryptoData?,
    val tether: CryptoData?,
    val solana: CryptoData?,
    val dogecoin: CryptoData?,
    val cardano: CryptoData?
)

data class CryptoData(
    val usd: Double = 0.0,
    val pln: Double = 0.0,
    val eur: Double = 0.0
)

data class MarketChartResponse(
    val prices: List<List<Double>>
)