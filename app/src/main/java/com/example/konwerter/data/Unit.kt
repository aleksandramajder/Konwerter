package com.example.konwerter.data

import java.math.BigDecimal

data class ConversionUnit(
    val name: String,
    val symbol: String,
    val toBase: BigDecimal,
    val category: Category
)