package com.example.konwerter.data

object Converter {

    fun convert(value: Double, fromUnit: Unit, toUnit: Unit): Double {
        if (fromUnit.category != toUnit.category) {
            throw IllegalArgumentException("Nie można konwertować między różnymi kategoriami")
        }

        if (fromUnit.category == Category.TEMPERATURE) {
            return convertTemperature(value, fromUnit, toUnit)
        }

        val baseValue = value * fromUnit.toBase

        return baseValue / toUnit.toBase
    }

    private fun convertTemperature(value: Double, fromUnit: Unit, toUnit: Unit): Double {
        val celsius = when (fromUnit.symbol) {
            "°C" -> value
            "°F" -> (value - 32) * 5.0 / 9.0
            "K" -> value - 273.15
            else -> value
        }

        return when (toUnit.symbol) {
            "°C" -> celsius
            "°F" -> celsius * 9.0 / 5.0 + 32
            "K" -> celsius + 273.15
            else -> celsius
        }
    }
}