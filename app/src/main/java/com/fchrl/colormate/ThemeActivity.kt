package com.fchrl.colormate

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.fchrl.colormate.databinding.ActivityThemeBinding

class ThemeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityThemeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityThemeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setIconBasedOnTheme()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)
    }

    private fun setupClickListeners() {
        // Click listener untuk tombol back
        binding.backBtn.setOnClickListener {
            finish() // Tutup activity ini dan kembali ke activity sebelumnya
        }

        binding.lightMode.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            recreate()
        }

        // Optional: Tambahkan click listener untuk card lainnya
        binding.darkMode.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            recreate()
        }

        binding.asSystem.setOnClickListener {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            recreate() // Reload activity untuk menerapkan perubahan tema
        }
    }

    private fun setIconBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val logoRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.logo_light // Logo untuk dark mode
        } else {
            R.drawable.logo        // Logo untuk light mode
        }

        val systemThemeRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.ic_system_light // Icon untuk dark mode
        } else {
            R.drawable.ic_system_dark // Icon untuk light mode
        }

        binding.logo.setImageResource(logoRes)
        binding.asSystemTheme.setImageResource(systemThemeRes)
    }

    private fun setBackgroundBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val backgroundRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.app_bg_inv // Background untuk dark mode
        } else {
            R.drawable.app_bg     // Background untuk light mode
        }

        // Set background pada root layout
        binding.main.setBackgroundResource(backgroundRes)
    }

    /**
     * Handle perubahan konfigurasi (rotation, theme change)
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Update logo ketika tema berubah
        setIconBasedOnTheme()
        //update bg ketika tema berubah
        setBackgroundBasedOnTheme()
        // Update status bar ketika tema berubah
        StatusBarHelper.setupStatusBar(this)
    }
}