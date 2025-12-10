package com.example.konwerter.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

object PreferencesManager {
    private const val PREF_NAME = "KonwerterPrefs"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_COLOR_THEME = "color_theme"
    private const val KEY_DECIMAL_PLACES = "decimal_places"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun getThemeMode(context: Context): Int {
        return getPrefs(context).getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }

    fun setThemeMode(context: Context, mode: Int) {
        getPrefs(context).edit().putInt(KEY_THEME_MODE, mode).apply()
    }

    fun getColorTheme(context: Context): String {
        return getPrefs(context).getString(KEY_COLOR_THEME, "Blue") ?: "Blue"
    }

    fun setColorTheme(context: Context, themeName: String) {
        getPrefs(context).edit().putString(KEY_COLOR_THEME, themeName).apply()
    }

    fun getDecimalPlaces(context: Context): Int {
        return getPrefs(context).getInt(KEY_DECIMAL_PLACES, 8)
    }

    fun setDecimalPlaces(context: Context, places: Int) {
        getPrefs(context).edit().putInt(KEY_DECIMAL_PLACES, places).apply()
    }
}