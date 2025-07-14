package com.fchrl.colormate

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fchrl.colormate.auth.ResetPasswordActivity
import com.fchrl.colormate.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setLogoBasedOnTheme()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)
    }

    private fun setupClickListeners() {
        // Click listener untuk card deteksi warna
        binding.changePassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }

        // Click listener untuk tombol back
        binding.backBtn.setOnClickListener {
            finish() // Tutup activity ini dan kembali ke activity sebelumnya
        }

        // Optional: Tambahkan click listener untuk card lainnya
        binding.changeTheme.setOnClickListener {
            val intent = Intent(this, ThemeActivity::class.java)
            startActivity(intent)
        }

        binding.bantuan.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }

        binding.about.setOnClickListener {
            val intent = Intent(this, InfoPembuatActivity::class.java)
            startActivity(intent)
        }

        binding.logOut.setOnClickListener {
            showLogoutConfirmationDialog()
        }
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

    private fun showLogoutConfirmationDialog() {
        val iconColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
        val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Konfirmasi Logout")
            .setMessage("Apakah Anda yakin ingin keluar dari aplikasi?")
            .setIcon(R.drawable.ic_logout)
            .setPositiveButton("Ya") { _, _ ->
                val intent = Intent(this, FrontActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Tidak") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()

        // Apply theme-responsive icon color
        dialog.findViewById<android.widget.ImageView>(android.R.id.icon)?.setColorFilter(iconColor)

        // Set "Ya" button to red
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(getColor(R.color.md_theme_error))
    }
}