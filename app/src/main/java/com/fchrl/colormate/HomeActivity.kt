package com.fchrl.colormate

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fchrl.colormate.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setLogoBasedOnTheme()
        setupClickListeners()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)

        setupBackPressHandler()
    }


    private fun setupClickListeners() {
        // Click listener untuk card deteksi warna
        binding.colorDetectCard.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Click listener untuk tombol back
        binding.backBtn.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Optional: Tambahkan click listener untuk card lainnya
        binding.colorBlindQuizCard.setOnClickListener {
            val intent = Intent(this, ColorBlindQuizActivity::class.java)
            startActivity(intent)
        }

        binding.historyCard.setOnClickListener {
            val intent = Intent(this, QuizHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.settingsCard.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Menampilkan dialog konfirmasi logout
     */
    private fun showLogoutConfirmationDialog() {
        // Opsi 1: AlertDialog dengan Material Design theme dan custom styling
        val iconColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog) // Gunakan custom theme
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setIcon(R.drawable.ic_logout) // Tambahkan icon (pastikan ada di drawable)
            .setPositiveButton("Ya") { _, _ ->
                // User mengkonfirmasi logout
                val intent = Intent(this, FrontActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .create()

        // Customize tampilan setelah dialog dibuat
        dialog.show()

        // Customize warna tombol
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.md_theme_error))
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(getColor(R.color.md_theme_primary))

        // Apply theme-responsive icon color
        dialog.findViewById<android.widget.ImageView>(android.R.id.icon)?.setColorFilter(iconColor)
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showLogoutConfirmationDialog()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }


    private fun setLogoBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val logoRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.logo_light // Logo untuk dark mode
        } else {
            R.drawable.logo        // Logo untuk light mode
        }

        binding.logo.setImageResource(logoRes)
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
        setLogoBasedOnTheme()
        //update bg ketika tema berubah
        setBackgroundBasedOnTheme()
        // Update status bar ketika tema berubah
        StatusBarHelper.setupStatusBar(this)
    }
}