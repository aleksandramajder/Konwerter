package com.example.konwerter

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.example.konwerter.data.Category
import com.example.konwerter.databinding.ActivityMainBinding
import com.example.konwerter.ui.adapters.CategoryPagerAdapter
import com.example.konwerter.utils.PreferencesManager
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var appliedTheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        setupViewPager()
    }

    override fun onRestart() {
        super.onRestart()
        val currentTheme = PreferencesManager.getColorTheme(this)
        if (currentTheme != appliedTheme) {
            recreate()
        }
    }

    private fun applyTheme() {
        val themeMode = PreferencesManager.getThemeMode(this)
        AppCompatDelegate.setDefaultNightMode(themeMode)

        val colorTheme = PreferencesManager.getColorTheme(this)
        appliedTheme = colorTheme // Store the theme name
        
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager() {
        val categories = Category.entries

        val adapter = CategoryPagerAdapter(this, categories)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = categories[position].displayName
            tab.setIcon(categories[position].iconResId)
        }.attach()
    }
}