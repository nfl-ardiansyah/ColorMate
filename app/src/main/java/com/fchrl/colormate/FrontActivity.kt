package com.fchrl.colormate
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.fchrl.colormate.auth.LoginActivity
import com.fchrl.colormate.databinding.ActivityFrontBinding
import com.fchrl.colormate.auth.RegisterActivity
import com.google.firebase.auth.FirebaseAuth

class FrontActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFrontBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        binding = ActivityFrontBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setupClickListeners()
        setLogoBasedOnTheme()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)

        splashScreen.setKeepOnScreenCondition {
            // Return true untuk keep splash screen
            // Return false untuk dismiss splash screen
            false
        }
    }

    private fun setupClickListeners() {
        binding.registBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.tutorialText.setOnClickListener {
            val intent = Intent(this, HelpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setLogoBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val logoRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.logo_light
        } else {
            R.drawable.logo
        }
        binding.logo.setImageResource(logoRes)
    }

    private fun setBackgroundBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val backgroundRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.app_bg_inv
        } else {
            R.drawable.app_bg
        }
        binding.main.setBackgroundResource(backgroundRes)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setLogoBasedOnTheme()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)
    }
}
