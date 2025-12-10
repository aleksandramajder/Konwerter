package com.example.konwerter.data

object UnitRepository {

    fun getUnitsForCategory(category: Category): List<Unit> {
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

    private fun getLengthUnits() = listOf(
        Unit("Milimetr", "mm", 0.001, Category.LENGTH),
        Unit("Centymetr", "cm", 0.01, Category.LENGTH),
        Unit("Metr", "m", 1.0, Category.LENGTH),
        Unit("Kilometr", "km", 1000.0, Category.LENGTH),
        Unit("Cal", "in", 0.0254, Category.LENGTH),
        Unit("Stopa", "ft", 0.3048, Category.LENGTH),
        Unit("Jard", "yd", 0.9144, Category.LENGTH),
        Unit("Mila", "mi", 1609.34, Category.LENGTH)
    )

    private fun getMassUnits() = listOf(
        Unit("Miligram", "mg", 0.000001, Category.MASS),
        Unit("Gram", "g", 0.001, Category.MASS),
        Unit("Dekagram","dkg", 0.01, Category.MASS),
        Unit("Kilogram", "kg", 1.0, Category.MASS),
        Unit("Tona", "t", 1000.0, Category.MASS),
    )

    private fun getTemperatureUnits() = listOf(
        Unit("Celsius", "°C", 1.0, Category.TEMPERATURE),
        Unit("Fahrenheit", "°F", 1.0, Category.TEMPERATURE),
        Unit("Kelvin", "K", 1.0, Category.TEMPERATURE)
    )

    private fun getVolumeUnits() = listOf(
        Unit("Milimetr sześcienny", "mm³", 0.000001, Category.VOLUME),
        Unit("Centymetr sześcienny", "cm³", 0.001, Category.VOLUME),
        Unit("Mililitr", "ml", 0.001, Category.VOLUME),
        Unit("Litr", "l", 1.0, Category.VOLUME),
        Unit("Metr sześcienny", "m³", 1000.0, Category.VOLUME),
        Unit("Uncja płynna (US)", "fl oz", 0.0295735, Category.VOLUME),
        Unit("Galon (US)", "gal", 3.78541, Category.VOLUME)
    )

    private fun getAreaUnits() = listOf(
        Unit("Milimetr kwadratowy", "mm²", 0.000001, Category.AREA),
        Unit("Centymetr kwadratowy", "cm²", 0.0001, Category.AREA),
        Unit("Metr kwadratowy", "m²", 1.0, Category.AREA),
        Unit("Kilometr kwadratowy", "km²", 1000000.0, Category.AREA),
        Unit("Hektar", "ha", 10000.0, Category.AREA),
        Unit("Ar", "a", 100.0, Category.AREA),
    )

    private fun getSpeedUnits() = listOf(
        Unit("Milimetr na sekundę", "mm/s", 0.001, Category.SPEED),
        Unit("Centymetr na sekundę", "cm/s", 0.01, Category.SPEED),
        Unit("Metr na sekundę", "m/s", 1.0, Category.SPEED),
        Unit("Metr na minutę", "m/min", 0.0166667, Category.SPEED),
        Unit("Kilometr na godzinę", "km/h", 0.277778, Category.SPEED),
        Unit("Mila na godzinę", "mph", 0.44704, Category.SPEED),
        Unit("Mach (prędkość dźwięku w powietrzu)", "Ma", 343.0, Category.SPEED),
        Unit("Prędkość światła (w próżni)", "c", 299792458.0, Category.SPEED)
    )

    private fun getTimeUnits() = listOf(
        Unit("Milisekunda", "ms", 0.001, Category.TIME),
        Unit("Sekunda", "s", 1.0, Category.TIME),
        Unit("Minuta", "min", 60.0, Category.TIME),
        Unit("Kwadrans", "15 min", 900.0, Category.TIME),
        Unit("Godzina", "h", 3600.0, Category.TIME),
        Unit("Dzień", "d", 86400.0, Category.TIME),
        Unit("Tydzień", "wk", 604800.0, Category.TIME),
        Unit("Miesiąc (30 dni)", "mth", 2592000.0, Category.TIME),
        Unit("Rok", "yr", 31536000.0, Category.TIME),
    )
}