package com.example.konwerter

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.konwerter.databinding.SettingsActivityBinding
import com.example.konwerter.utils.PreferencesManager

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupThemeMode()
        setupColorTheme()
        setupDecimalPlaces()

        binding.btnDone.setOnClickListener {
            finish()
        }

        binding.privacyPolicyButton.setOnClickListener {
            startActivity(Intent(this, PrivacyActivity::class.java))
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun applyTheme() {
        val themeMode = PreferencesManager.getThemeMode(this)
        AppCompatDelegate.setDefaultNightMode(themeMode)

        val colorTheme = PreferencesManager.getColorTheme(this)
        val themeId = when (colorTheme) {
            "Blue" -> R.style.Theme_Konwerter_Blue
            "Green" -> R.style.Theme_Konwerter_Green
            "Orange" -> R.style.Theme_Konwerter_Orange
            "Red" -> R.style.Theme_Konwerter_Red
            "Purple" -> R.style.Theme_Konwerter_Purple
            else -> R.style.Theme_Konwerter_Blue
        }
        setTheme(themeId)
    }

    private fun setupThemeMode() {
        val currentMode = PreferencesManager.getThemeMode(this)
        binding.toggleThemeMode.check(
            when (currentMode) {
                AppCompatDelegate.MODE_NIGHT_NO -> R.id.btnThemeLight
                AppCompatDelegate.MODE_NIGHT_YES -> R.id.btnThemeDark
                else -> R.id.btnThemeSystem
            }
        )

        binding.toggleThemeMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val mode = when (checkedId) {
                    R.id.btnThemeLight -> AppCompatDelegate.MODE_NIGHT_NO
                    R.id.btnThemeDark -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }
                PreferencesManager.setThemeMode(this, mode)
                AppCompatDelegate.setDefaultNightMode(mode)
            }
        }
    }

    private fun setupColorTheme() {
        val currentColor = PreferencesManager.getColorTheme(this)
        updateColorSelection(currentColor)

        binding.colorBlue.setOnClickListener { setAppTheme("Blue") }
        binding.colorGreen.setOnClickListener { setAppTheme("Green") }
        binding.colorOrange.setOnClickListener { setAppTheme("Orange") }
        binding.colorRed.setOnClickListener { setAppTheme("Red") }
        binding.colorPurple.setOnClickListener { setAppTheme("Purple") }
    }

    private fun updateColorSelection(color: String) {
        binding.colorBlue.alpha = if (color == "Blue") 1.0f else 0.5f
        binding.colorGreen.alpha = if (color == "Green") 1.0f else 0.5f
        binding.colorOrange.alpha = if (color == "Orange") 1.0f else 0.5f
        binding.colorRed.alpha = if (color == "Red") 1.0f else 0.5f
        binding.colorPurple.alpha = if (color == "Purple") 1.0f else 0.5f
    }

    private fun setAppTheme(color: String) {
        PreferencesManager.setColorTheme(this, color)
        recreate()
    }

    private fun setupDecimalPlaces() {
        val places = listOf("2", "4", "6", "8", "10")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, places)
        binding.autoCompleteDecimalPlaces.setAdapter(adapter)

        val currentDecimal = PreferencesManager.getDecimalPlaces(this)
        binding.autoCompleteDecimalPlaces.setText(currentDecimal.toString(), false)

        binding.autoCompleteDecimalPlaces.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selected = places[position].toInt()
            PreferencesManager.setDecimalPlaces(this@SettingsActivity, selected)
        }
    }
}