package com.fchrl.colormate

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fchrl.colormate.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)
    }

    private fun setupClickListeners() {
        // Click listener untuk card deteksi warna
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.colorDetectCard.setOnClickListener {
            intent = Intent(this, ColorScannerHelpActivity::class.java)
            startActivity(intent)
        }
        binding.colorBlindQuizCard.setOnClickListener {
            intent = Intent(this, ColorQuizHelpActivity::class.java)
            startActivity(intent)
        }
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

        //update bg ketika tema berubah
        setBackgroundBasedOnTheme()
        // Update status bar ketika tema berubah
        StatusBarHelper.setupStatusBar(this)
    }
}