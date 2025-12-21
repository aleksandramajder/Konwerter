package com.example.konwerter.data

import java.math.BigDecimal
import java.math.MathContext

object Converter {

    private val MC = MathContext.DECIMAL128

    fun convert(value: BigDecimal, fromUnit: ConversionUnit, toUnit: ConversionUnit): BigDecimal {
        if (fromUnit.category != toUnit.category) {
            throw IllegalArgumentException("Nie można konwertować między różnymi kategoriami")
        }

        if (fromUnit.category == Category.TEMPERATURE) {
            return convertTemperature(value, fromUnit, toUnit)
        }


        val baseValue = value.multiply(fromUnit.toBase, MC)
        return baseValue.divide(toUnit.toBase, MC)
    }

    private fun convertTemperature(value: BigDecimal, fromUnit: ConversionUnit, toUnit: ConversionUnit): BigDecimal {
        val offsetF = BigDecimal("32")
        val offsetK = BigDecimal("273.15")
        val five = BigDecimal("5")
        val nine = BigDecimal("9")

        val celsius = when (fromUnit.symbol) {
            "°C" -> value
            "°F" -> value.subtract(offsetF, MC).multiply(five, MC).divide(nine, MC) // (F - 32) * 5 / 9
            "K" -> {
                if (value < BigDecimal.ZERO) throw IllegalArgumentException("Temperatura w Kelwinach nie może być ujemna")
                value.subtract(offsetK, MC) // K - 273.15
            }
            else -> value
        }

        return when (toUnit.symbol) {
            "°C" -> celsius
            "°F" -> celsius.multiply(nine, MC).divide(five, MC).add(offsetF, MC) // (C * 9 / 5) + 32
            "K" -> {
                val kelvin = celsius.add(offsetK, MC) // C + 273.15
                if (kelvin < BigDecimal.ZERO) throw IllegalArgumentException("Wynik nie może być ujemny w Kelwinach")
                kelvin
            }
            else -> celsius
        }
    }
}