package com.example.konwerter.data

data class ConversionUnit(
    val name: String,
    val symbol: String,
    val toBase: Double,
    val category: Category
)