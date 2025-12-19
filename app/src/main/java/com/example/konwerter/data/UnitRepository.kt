package com.example.konwerter.data

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
            ConversionUnit("Metr", "m", 1.0, Category.LENGTH),
            ConversionUnit("Kilometr", "km", 1000.0, Category.LENGTH),
            ConversionUnit("Centymetr", "cm", 0.01, Category.LENGTH),
            ConversionUnit("Milimetr", "mm", 0.001, Category.LENGTH),
            ConversionUnit("Mila", "mi", 1609.34, Category.LENGTH),
            ConversionUnit("Jard", "yd", 0.9144, Category.LENGTH),
            ConversionUnit("Stopa", "ft", 0.3048, Category.LENGTH),
            ConversionUnit("Cal", "in", 0.0254, Category.LENGTH)
        )
    }

    private fun getMassUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Gram", "g", 1.0, Category.MASS),
            ConversionUnit("Kilogram", "kg", 1000.0, Category.MASS),
            ConversionUnit("Miligram", "mg", 0.001, Category.MASS),
            ConversionUnit("Tona", "t", 1000000.0, Category.MASS),
            ConversionUnit("Funt", "lb", 453.592, Category.MASS),
            ConversionUnit("Uncja", "oz", 28.3495, Category.MASS)
        )
    }

    private fun getTemperatureUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Celsjusz", "°C", 0.0, Category.TEMPERATURE),
            ConversionUnit("Fahrenheit", "°F", 0.0, Category.TEMPERATURE),
            ConversionUnit("Kelwin", "K", 0.0, Category.TEMPERATURE)
        )
    }

    private fun getVolumeUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Litr", "l", 1.0, Category.VOLUME),
            ConversionUnit("Mililitr", "ml", 0.001, Category.VOLUME),
            ConversionUnit("Galon", "gal", 3.78541, Category.VOLUME)
        )
    }

    private fun getAreaUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Metr kwadratowy", "m²", 1.0, Category.AREA),
            ConversionUnit("Kilometr kwadratowy", "km²", 1000000.0, Category.AREA),
            ConversionUnit("Hektar", "ha", 10000.0, Category.AREA),
            ConversionUnit("Ar", "a", 100.0, Category.AREA)
        )
    }

    private fun getSpeedUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Metry na sekundę", "m/s", 1.0, Category.SPEED),
            ConversionUnit("Kilometry na godzinę", "km/h", 0.277778, Category.SPEED),
            ConversionUnit("Mile na godzinę", "mph", 0.44704, Category.SPEED)
        )
    }

    private fun getTimeUnits(): List<ConversionUnit> {
        return listOf(
            ConversionUnit("Sekunda", "s", 1.0, Category.TIME),
            ConversionUnit("Minuta", "min", 60.0, Category.TIME),
            ConversionUnit("Godzina", "h", 3600.0, Category.TIME),
            ConversionUnit("Dzień", "d", 86400.0, Category.TIME)
        )
    }
}