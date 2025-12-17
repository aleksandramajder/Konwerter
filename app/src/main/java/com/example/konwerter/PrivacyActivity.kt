package com.example.konwerter

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.konwerter.databinding.ActivityPrivacyBinding
import com.example.konwerter.utils.PreferencesManager

class PrivacyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPrivacyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        applyTheme()
        binding = ActivityPrivacyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val privacyPolicyHtml = getString(R.string.privacy_policy_html)
        binding.webView.loadDataWithBaseURL(null, privacyPolicyHtml, "text/html", "utf-8", null)
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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}