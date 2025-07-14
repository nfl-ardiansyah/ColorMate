package com.fchrl.colormate.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fchrl.colormate.R
import com.fchrl.colormate.StatusBarHelper
import com.fchrl.colormate.databinding.ActivityResetPasswordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "ResetPasswordActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setLogoBasedOnTheme()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)

        setupListeners()
        setupFormValidation()
    }


    private fun setupListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.resetPasswordBtn.setOnClickListener {
            sendResetPasswordEmail()
        }
    }

    private fun setupFormValidation() {
        binding.usernameLoginField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validateEmail()
                updateButtonState()
            }
        })
    }

    private fun validateEmail(): Boolean {
        val email = binding.usernameLoginField.text.toString().trim()

        when {
            email.isEmpty() -> {
                binding.usernameLayout.error = "Email tidak boleh kosong"
                return false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.usernameLayout.error = "Format email tidak valid"
                return false
            }
            else -> {
                binding.usernameLayout.error = null
                return true
            }
        }
    }

    private fun updateButtonState() {
        binding.resetPasswordBtn.isEnabled = validateEmail()
    }

    private fun sendResetPasswordEmail() {
        if (!validateEmail()) {
            Toast.makeText(this, "Harap masukkan email yang valid", Toast.LENGTH_SHORT).show()
            return
        }

        val email = binding.usernameLoginField.text.toString().trim().lowercase() // Convert to lowercase
        Log.d(TAG, "Attempting to send reset email to: $email")

        showLoading(true)

        // Langsung kirim email reset tanpa cek fetchSignInMethodsForEmail
        // karena method tersebut deprecated dan tidak reliable
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                showLoading(false)
                Log.d(TAG, "Reset email result - Success: ${task.isSuccessful}")

                if (task.isSuccessful) {
                    Log.d(TAG, "Reset email sent successfully to: $email")
                    showSuccessDialog(email)
                } else {
                    val exception = task.exception
                    Log.e(TAG, "Failed to send reset email", exception)

                    val errorMessage = when (exception) {
                        is FirebaseAuthInvalidUserException -> {
                            Log.d(TAG, "Invalid user exception for email: $email")
                            "Email tidak terdaftar di sistem. Pastikan email yang Anda masukkan sudah terdaftar."
                        }
                        else -> {
                            Log.d(TAG, "Other exception: ${exception?.message}")
                            "Gagal mengirim email reset. Pastikan koneksi internet Anda stabil dan email yang dimasukkan benar."
                        }
                    }

                    showErrorDialog("Gagal Mengirim Email", errorMessage)
                }
            }
    }

    private fun showSuccessDialog(email: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Email Reset Terkirim!")
        builder.setMessage(
            "Link reset password telah dikirim ke:\n\n$email\n\n" +
                    "Langkah selanjutnya:\n" +
                    "1. Buka email Anda (cek juga folder spam/junk)\n" +
                    "2. Klik link reset password\n" +
                    "3. Masukkan password baru\n" +
                    "4. Login dengan password baru\n\n" +
                    "Link akan kadaluarsa dalam 1 jam."
        )
        builder.setPositiveButton("Mengerti") { _, _ ->
            finish()
        }
        builder.setNeutralButton("Buka Email") { _, _ ->
            // Buka aplikasi email default
            try {
                val intent = android.content.Intent(android.content.Intent.ACTION_MAIN)
                intent.addCategory(android.content.Intent.CATEGORY_APP_EMAIL)
                startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Silakan buka aplikasi email Anda secara manual", Toast.LENGTH_SHORT).show()
            }
            finish()
        }
        builder.setCancelable(false)
        builder.show()
    }

    private fun showErrorDialog(title: String, message: String) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        // Tambahkan button untuk debug info
        builder.setNeutralButton("Debug Info") { _, _ ->
            showDebugDialog()
        }
        builder.show()
    }

    private fun showDebugDialog() {
        val currentUser = auth.currentUser
        val debugInfo = buildString {
            append("Debug Information:\n\n")
            append("Current User: ${currentUser?.email ?: "None"}\n")
            append("Email yang dimasukkan: ${binding.usernameLoginField.text.toString().trim()}\n")
            append("Firebase Project ID: ${auth.app.options.projectId}\n")
            append("App Name: ${auth.app.name}\n")
        }

        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Debug Information")
        builder.setMessage(debugInfo)
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()

        Log.d(TAG, debugInfo)
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.resetPasswordBtn.isEnabled = !show
        binding.usernameLoginField.isEnabled = !show

        if (show) {
            binding.resetPasswordBtn.text = "Mengirim..."
        } else {
            binding.resetPasswordBtn.text = "Kirim Email Reset Password"
        }
    }

    private fun setLogoBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val logoRes = if (currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.logo_light
        } else {
            R.drawable.logo
        }
        binding.logo.setImageResource(logoRes)
    }

    private fun setBackgroundBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val backgroundRes = if (currentMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.app_bg_inv
        } else {
            R.drawable.app_bg
        }
        binding.main.setBackgroundResource(backgroundRes)
    }
}