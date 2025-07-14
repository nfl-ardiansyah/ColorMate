package com.fchrl.colormate.auth

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fchrl.colormate.HomeActivity
import com.fchrl.colormate.R
import com.fchrl.colormate.StatusBarHelper
import com.fchrl.colormate.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setLogoBasedOnTheme()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)

        setupListeners()
    }

    private fun setupListeners() {
        binding.loginBtn.setOnClickListener {
            performLogin()
        }

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.textDaftar.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.forgotPassword.setOnClickListener {
            val intent = Intent(this, ResetPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin() {
        val input = binding.usernameLoginField.text.toString().trim()
        val password = binding.passwordLoginField.text.toString().trim()

        // Validasi input
        if (TextUtils.isEmpty(input)) {
            binding.usernameLoginField.error = "Username atau Email tidak boleh kosong"
            binding.usernameLoginField.requestFocus()
            return
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordLoginField.error = "Kata sandi tidak boleh kosong"
            binding.passwordLoginField.requestFocus()
            return
        }

        if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
            // Jika input adalah email
            loginWithEmail(input, password)
        } else {
            // Jika input adalah username
            fetchEmailFromUsername(input, password)
        }
    }

    private fun loginWithEmail(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToHome()
                } else {
                    Toast.makeText(this, "Login Gagal: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchEmailFromUsername(username: String, password: String) {
        firestore.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val email = documents.documents[0].getString("email")
                    if (!email.isNullOrEmpty()) {
                        // Lakukan login dengan email yang ditemukan
                        loginWithEmail(email, password)
                    } else {
                        Toast.makeText(this, "Email tidak ditemukan untuk username ini.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Username tidak ditemukan.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Gagal mengambil data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    // Kode untuk UI JANGAN DIUBAH
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
