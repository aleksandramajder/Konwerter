package com.example.konwerter.data

import com.example.konwerter.R

enum class Category(val displayName: String, val iconResId: Int) {
    LENGTH("Długość", R.drawable.ic_length),
    MASS("Masa", R.drawable.ic_mass),
    TEMPERATURE("Temperatura", R.drawable.ic_temperature),
    VOLUME("Objętość", R.drawable.ic_volume),
    AREA("Powierzchnia", R.drawable.ic_area),
    SPEED("Prędkość", R.drawable.ic_speed),
    TIME("Czas", R.drawable.ic_time),
    CRYPTO("Kryptowaluty", R.drawable.ic_currency_bitcoin)
}