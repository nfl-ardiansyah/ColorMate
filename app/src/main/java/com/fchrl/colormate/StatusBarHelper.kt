package com.fchrl.colormate

import android.app.Activity
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.core.content.ContextCompat

object StatusBarHelper {

    /**
     * Set status bar untuk activity apapun
     */
    @JvmStatic
    fun setupStatusBar(activity: Activity, forceLight: Boolean? = null) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val isDarkMode = forceLight?.let { !it } ?: run {
                when (activity.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                    Configuration.UI_MODE_NIGHT_YES -> true
                    else -> false
                }
            }

            // Set warna background status bar - pakai warna yang sama seperti kode asli kamu
            activity.window.statusBarColor = if (isDarkMode) {
                ContextCompat.getColor(activity, R.color.md_theme_scrim)
            } else {
                ContextCompat.getColor(activity, R.color.md_theme_onPrimary)
            }

            // Set warna icon status bar
            setStatusBarIconColor(activity, isDarkMode)
        }
    }

    /**
     * Set warna icon status bar
     */
    private fun setStatusBarIconColor(activity: Activity, isDarkMode: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity.window.insetsController?.setSystemBarsAppearance(
                if (isDarkMode) 0 else WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = if (isDarkMode) {
                activity.window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                activity.window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    /**
     * Force light status bar (icon hitam)
     */
    @JvmStatic
    fun setLightStatusBar(activity: Activity) {
        setupStatusBar(activity, forceLight = true)
    }

    /**
     * Force dark status bar (icon putih)
     */
    @JvmStatic
    fun setDarkStatusBar(activity: Activity) {
        setupStatusBar(activity, forceLight = false)
    }
}