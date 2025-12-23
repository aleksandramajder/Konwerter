package com.example.konwerter.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CryptoApiService {
    @GET("simple/price?ids=bitcoin,ethereum,tether,solana,dogecoin,cardano&vs_currencies=usd,pln,eur")
    suspend fun getCryptoPrices(): CoinGeckoResponse

    @GET("coins/{id}/market_chart")
    suspend fun getMarketChart(
        @Path("id") id: String,
        @Query("vs_currency") vsCurrency: String,
        @Query("days") days: String
    ): MarketChartResponse
}