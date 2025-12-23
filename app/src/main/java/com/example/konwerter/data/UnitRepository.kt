package com.example.konwerter.data

import java.math.BigDecimal

object UnitRepository {
    fun getUnitsForCategory(category: Category): List<ConversionUnit> {
        return when (category) {
            Category.LENGTH -> getLengthUnits()
            Category.MASS -> getMassUnits()
            Category.TEMPERATURE -> getTemperatureUnits()
            Category.VOLUME -> getVolumeUnits()
            Category.AREA -> getAreaUnits()
            Category.SPEED -> getSpeedUnits()
            Category.TIME -> getTimeUnits()
            Category.CRYPTO -> emptyList()
        }
    }

    private fun getLengthUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Metr", "m", BigDecimal.ONE, Category.LENGTH),
            ConversionUnit("Kilometr", "km", BigDecimal("1000"), Category.LENGTH),
            ConversionUnit("Decymetr", "dm", BigDecimal("0.1"), Category.LENGTH),
            ConversionUnit("Centymetr", "cm", BigDecimal("0.01"), Category.LENGTH),
            ConversionUnit("Milimetr", "mm", BigDecimal("0.001"), Category.LENGTH),
            ConversionUnit("Mikrometr", "µm", BigDecimal("0.000001"), Category.LENGTH),
            ConversionUnit("Nanometr", "nm", BigDecimal("0.000000001"), Category.LENGTH),
            ConversionUnit("Mila lądowa", "mi", BigDecimal("1609.344"), Category.LENGTH),
            ConversionUnit("Mila morska", "nmi", BigDecimal("1852"), Category.LENGTH),
            ConversionUnit("Jard", "yd", BigDecimal("0.9144"), Category.LENGTH),
            ConversionUnit("Stopa", "ft", BigDecimal("0.3048"), Category.LENGTH),
            ConversionUnit("Cal", "in", BigDecimal("0.0254"), Category.LENGTH)
        )
    }

    private fun getMassUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Gram", "g", BigDecimal("1.0"), Category.MASS),
            ConversionUnit("Kilogram", "kg", BigDecimal("1000.0"), Category.MASS),
            ConversionUnit("Miligram", "mg", BigDecimal("0.001"), Category.MASS),
            ConversionUnit("Tona", "t", BigDecimal("1000000.0"), Category.MASS),
            ConversionUnit("Kwintal", "q", BigDecimal("100000.0"), Category.MASS),
            ConversionUnit("Miligram", "mg", BigDecimal("0.001"), Category.MASS),
            ConversionUnit("Mikrogram", "µg", BigDecimal("0.000001"), Category.MASS),
            ConversionUnit("Funt", "lb", BigDecimal("453.592"), Category.MASS),
            ConversionUnit("Uncja", "oz", BigDecimal("28.3495"), Category.MASS),
            ConversionUnit("Uncja trojańska", "oz t", BigDecimal("31.1"), Category.MASS),
            ConversionUnit("Karat", "ct", BigDecimal("0.2"), Category.MASS)
        )
    }

    private fun getTemperatureUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Celsjusz", "°C", BigDecimal("0.0"), Category.TEMPERATURE),
            ConversionUnit("Fahrenheit", "°F", BigDecimal("0.0"), Category.TEMPERATURE),
            ConversionUnit("Kelwin", "K", BigDecimal("0.0"), Category.TEMPERATURE)
        )
    }

    private fun getVolumeUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Litr", "l", BigDecimal("1.0"), Category.VOLUME),
            ConversionUnit("Mililitr", "ml", BigDecimal("0.001"), Category.VOLUME),
            ConversionUnit("Metr sześcienny", "m³", BigDecimal("1000.0"), Category.VOLUME),
            ConversionUnit("Centymetr sześcienny", "cm³", BigDecimal("0.001"), Category.VOLUME),
            ConversionUnit("Hektolitr", "hl", BigDecimal("100.0"), Category.VOLUME),
            ConversionUnit("Galon (US)", "gal", BigDecimal("3.785411784"), Category.VOLUME),
            ConversionUnit("Galon (UK)", "gal (uk)", BigDecimal("4.54609"), Category.VOLUME),
            ConversionUnit("Pinta (US)", "pt", BigDecimal("0.473176"), Category.VOLUME),
            ConversionUnit("Uncja płynu (US)", "fl oz", BigDecimal("0.0295735"), Category.VOLUME)
        )
    }

    private fun getAreaUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Metr kwadratowy", "m²", BigDecimal("1.0"), Category.AREA),
            ConversionUnit("Kilometr kwadratowy", "km²", BigDecimal("1000000.0"), Category.AREA),
            ConversionUnit("Centymetr kwadratowy", "cm²", BigDecimal("0.0001"), Category.AREA),
            ConversionUnit("Hektar", "ha", BigDecimal("10000.0"), Category.AREA),
            ConversionUnit("Ar", "a", BigDecimal("100.0"), Category.AREA),
            ConversionUnit("Akr", "ac", BigDecimal("4046.85642"), Category.AREA),
            ConversionUnit("Stopa kwadratowa", "ft²", BigDecimal("0.092903"), Category.AREA)
        )
    }

    private fun getSpeedUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Metry na sekundę", "m/s", BigDecimal("1.0"), Category.SPEED),
            ConversionUnit("Kilometry na godzinę", "km/h", BigDecimal("0.277778"), Category.SPEED),
            ConversionUnit("Kilometry na minutę", "km/min", BigDecimal("16.66666666666667"), Category.SPEED),
            ConversionUnit("Kilometry na sekundę", "km/s", BigDecimal("1000.0"), Category.SPEED),
            ConversionUnit("Mile na godzinę", "mph", BigDecimal("0.44704"), Category.SPEED),
            ConversionUnit("Węzeł", "kn", BigDecimal("0.514444444444"), Category.SPEED),
            ConversionUnit("Stopy na sekundę", "ft/s", BigDecimal("0.3048"), Category.SPEED)
        )
    }

    private fun getTimeUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Sekunda", "s", BigDecimal("1.0"), Category.TIME),
            ConversionUnit("Milisekunda", "ms", BigDecimal("0.001"), Category.TIME),
            ConversionUnit("Minuta", "min", BigDecimal("60.0"), Category.TIME),
            ConversionUnit("Godzina", "h", BigDecimal("3600.0"), Category.TIME),
            ConversionUnit("Dzień", "d", BigDecimal("86400.0"), Category.TIME),
            ConversionUnit("Tydzień", "wk", BigDecimal("604800.0"), Category.TIME),
            ConversionUnit("Rok (nieprzestępny)", "y", BigDecimal("31536000.0"), Category.TIME)
        )
    }
}