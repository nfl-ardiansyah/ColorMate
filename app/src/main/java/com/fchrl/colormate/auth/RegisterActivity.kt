package com.fchrl.colormate.auth

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.fchrl.colormate.R
import com.fchrl.colormate.StatusBarHelper
import com.fchrl.colormate.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setLogoBasedOnTheme()
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)

        emailFocusListener()
        usernameFocusListener()
        passwordFocusListener()
        confirmPasswordFocusListener()

        setupListeners()
    }

    private fun setupListeners() {
        binding.registBtn.setOnClickListener {
            performRegistration()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.textMasuk.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun performRegistration() {
        val email = binding.emailRegistField.text.toString().trim()
        val username = binding.usernameRegistField.text.toString().trim()
        val password = binding.passwordRegistField.text.toString().trim()
        val confirmPassword = binding.confirmPasswordField.text.toString().trim()

        // Input validation
        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Semua isian harus diisi.", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Kata sandi tidak sama.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a new user in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        saveUserToFirestore(uid, username, email)
                    } else {
                        Toast.makeText(this, "Gagal mendapatkan UID pengguna.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    handleRegistrationError(task.exception)
                }
            }
    }

    private fun handleRegistrationError(exception: Exception?) {
        try {
            throw exception!!
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Toast.makeText(this, "Email atau kata sandi tidak valid.", Toast.LENGTH_SHORT).show()
        } catch (e: FirebaseAuthUserCollisionException) {
            Toast.makeText(this, "Email ini sudah pernah didaftarkan.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserToFirestore(uid: String, username: String, email: String) {
        val userData = hashMapOf(
            "email" to email,
            "username" to username,
            "uid" to uid,
            "createdAt" to Timestamp.now()
        )

        firestore.collection("users")
            .document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d("FirestoreSuccess", "User data berhasil disimpan untuk UID: $uid")
                Toast.makeText(this, "Pendaftaran berhasil!", Toast.LENGTH_SHORT).show()
                navigateToLogin()

            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Gagal menyimpan data pengguna: ${e.message}")
                Toast.makeText(this, "Gagal menyimpan data pengguna: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun emailFocusListener() {
        binding.emailRegistField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.emailLayout.helperText = validEmail()
            }
        }
    }

    private fun validEmail(): String? {
        val emailText = binding.emailRegistField.text.toString()
        return if (!Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            "Email tidak valid"
        } else null
    }

    private fun usernameFocusListener() {
        binding.usernameRegistField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                validUsername()
            }
        }
    }

    private fun validUsername() {
        val usernameText = binding.usernameRegistField.text.toString()

        // Query Firestore untuk cek username
        firestore.collection("users")
            .whereEqualTo("username", usernameText)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    binding.usernameLayout.helperText = "Username sudah digunakan, coba yang lain."
                } else {
                    binding.usernameLayout.helperText = null
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this@RegisterActivity, "Error checking username: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun passwordFocusListener() {
        binding.passwordRegistField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.passwordLayout.helperText = validPassword()
            }
        }
    }

    private fun confirmPasswordFocusListener() {
        binding.confirmPasswordField.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                binding.confirmPasswordLayout.helperText = validConfirmPassword()
            }
        }
    }

    private fun validPassword(): String? {
        val passwordText = binding.passwordRegistField.text.toString()
        return when {
            passwordText.length < 6 -> "Minimal 6 karakter"
            !passwordText.matches(".*[A-Z].*".toRegex()) -> "Harus mengandung huruf besar"
            !passwordText.matches(".*[a-z].*".toRegex()) -> "Harus mengandung huruf kecil"
            !passwordText.matches(".*[0-9].*".toRegex()) -> "Harus mengandung angka"
            else -> null
        }
    }

    private fun validConfirmPassword(): String? {
        val passwordText = binding.passwordRegistField.text.toString()
        val confirmPasswordText = binding.confirmPasswordField.text.toString()

        return if (passwordText != confirmPasswordText) {
            "Kata sandi tidak sama"
        } else null
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun setLogoBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val logoRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.logo_light // Logo untuk dark mode
        } else {
            R.drawable.logo // Logo untuk light mode
        }

        binding.logo.setImageResource(logoRes)
    }

    private fun setBackgroundBasedOnTheme() {
        val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK

        val backgroundRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
            R.drawable.app_bg_inv // Background untuk dark mode
        } else {
            R.drawable.app_bg // Background untuk light mode
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
