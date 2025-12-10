package com.example.konwerter.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CryptoRepository {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.coingecko.com/api/v3/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(CryptoApiService::class.java)

    private var cachedPrices: Map<String, Double> = emptyMap()
    private var lastFetchTime: Long = 0
    private const val CACHE_DURATION = 60000L // 1 minuta

    suspend fun getCryptoUnits(): List<Unit> = withContext(Dispatchers.IO) {
        try {
            if (System.currentTimeMillis() - lastFetchTime < CACHE_DURATION && cachedPrices.isNotEmpty()) {
                return@withContext createUnitsFromCache()
            }

            val response = api.getCryptoPrices()

            val btcUsd = response.bitcoin?.usd ?: 0.0
            val btcPln = response.bitcoin?.pln ?: 0.0
            val btcEur = response.bitcoin?.eur ?: 0.0

            val plnRate = if (btcPln > 0) btcUsd / btcPln else 0.25
            val eurRate = if (btcEur > 0) btcUsd / btcEur else 1.08

            cachedPrices = mapOf(
                "BTC" to (response.bitcoin?.usd ?: 0.0),
                "ETH" to (response.ethereum?.usd ?: 0.0),
                "USDT" to (response.tether?.usd ?: 1.0),
                "SOL" to (response.solana?.usd ?: 0.0),
                "DOGE" to (response.dogecoin?.usd ?: 0.0),
                "ADA" to (response.cardano?.usd ?: 0.0),
                "PLN" to plnRate,
                "EUR" to eurRate
            )

            lastFetchTime = System.currentTimeMillis()

            createUnitsFromCache()

        } catch (e: Exception) {
            e.printStackTrace()
            getDefaultCryptoUnits()
        }
    }

    suspend fun getHistoricalData(cryptoUnit: Unit, targetCurrencyUnit: Unit, days: String = "7"): List<Pair<Long, Double>> = withContext(Dispatchers.IO) {
        try {
            val id = getCoinId(cryptoUnit.symbol)
            if (id == null) return@withContext emptyList()

            val vsCurrency = when(targetCurrencyUnit.symbol) {
                "PLN" -> "pln"
                "EUR" -> "eur"
                else -> "usd"
            }

            val response = api.getMarketChart(id, vsCurrency, days)
            response.prices.map { Pair(it[0].toLong(), it[1]) }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
    
    fun isCrypto(unit: Unit): Boolean {
        return getCoinId(unit.symbol) != null
    }

    private fun getCoinId(symbol: String): String? {
        return when (symbol) {
            "BTC" -> "bitcoin"
            "ETH" -> "ethereum"
            "USDT" -> "tether"
            "SOL" -> "solana"
            "DOGE" -> "dogecoin"
            "ADA" -> "cardano"
            else -> null
        }
    }

    private fun createUnitsFromCache(): List<Unit> {
        return listOf(
            Unit("US Dollar", "USD", 1.0, Category.CRYPTO),
            Unit("Polski Złoty", "PLN", cachedPrices["PLN"] ?: 0.25, Category.CRYPTO),
            Unit("Euro", "EUR", cachedPrices["EUR"] ?: 1.08, Category.CRYPTO),
            Unit("Bitcoin", "BTC", cachedPrices["BTC"] ?: 50000.0, Category.CRYPTO),
            Unit("Ethereum", "ETH", cachedPrices["ETH"] ?: 3000.0, Category.CRYPTO),
            Unit("Tether", "USDT", cachedPrices["USDT"] ?: 1.0, Category.CRYPTO),
            Unit("Solana", "SOL", cachedPrices["SOL"] ?: 100.0, Category.CRYPTO),
            Unit("Dogecoin", "DOGE", cachedPrices["DOGE"] ?: 0.1, Category.CRYPTO),
            Unit("Cardano", "ADA", cachedPrices["ADA"] ?: 0.5, Category.CRYPTO)
        )
    }

    private fun getDefaultCryptoUnits(): List<Unit> {
        return listOf(
            Unit("US Dollar", "USD", 1.0, Category.CRYPTO),
            Unit("Polski Złoty", "PLN", 0.25, Category.CRYPTO),
            Unit("Euro", "EUR", 1.08, Category.CRYPTO),
            Unit("Bitcoin", "BTC", 50000.0, Category.CRYPTO),
            Unit("Ethereum", "ETH", 3000.0, Category.CRYPTO),
            Unit("Tether", "USDT", 1.0, Category.CRYPTO)
        )
    }
}