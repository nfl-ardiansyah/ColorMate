package com.fchrl.colormate

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fchrl.colormate.databinding.ActivityQuizResultBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QuizHistoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        fetchQuizHistoryDetail()
        StatusBarHelper.setupStatusBar(this)
        setBackgroundBasedOnTheme()
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Sembunyikan tombol "Ulangi Kuis" karena ini hanya viewer
        binding.retakeQuizBtn.visibility = View.GONE
        binding.backToHomeBtn.setOnClickListener {
            finish()
        }
    }

    private fun fetchQuizHistoryDetail() {
        val user = FirebaseAuth.getInstance().currentUser
        val historyId = intent.getStringExtra("history_id")

        if (user == null || historyId == null) {
            Toast.makeText(this, "Gagal menampilkan detail riwayat", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(user.uid)
            .collection("history")
            .document(historyId)
            .get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Riwayat tidak ditemukan", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                val totalQuestions = document.getLong("total_questions")?.toInt() ?: 0
                val answeredQuestions = document.getLong("answered_questions")?.toInt() ?: 0
                val score = document.getLong("score")?.toInt() ?: 0
                val diagnosis = document.getString("diagnosis") ?: ""

                // Perbaikan untuk mengambil recommendations dari Firestore
                val recommendations = try {
                    // Coba ambil sebagai List<String> langsung
                    val recList = document.get("recommendations") as? List<*>
                    recList?.mapNotNull { it as? String } ?: emptyList()
                } catch (e: Exception) {
                    Log.w("QuizHistoryDetail", "Error parsing recommendations: ${e.message}")
                    // Fallback: coba ambil sebagai array string
                    try {
                        val recArray = document.get("recommendations") as? ArrayList<*>
                        recArray?.mapNotNull { it as? String } ?: emptyList()
                    } catch (e2: Exception) {
                        Log.w("QuizHistoryDetail", "Error parsing recommendations as array: ${e2.message}")
                        emptyList()
                    }
                }

                Log.d("QuizHistoryDetail", "Loaded recommendations: $recommendations")

                displayResults(
                    totalQuestions,
                    answeredQuestions,
                    score,
                    diagnosis,
                    recommendations
                )
            }
            .addOnFailureListener { e ->
                Log.e("QuizHistoryDetail", "Error fetching history detail", e)
                Toast.makeText(this, "Gagal memuat detail", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun displayResults(
        totalQuestions: Int,
        answeredQuestions: Int,
        score: Int,
        diagnosis: String,
        recommendations: List<String>
    ) {
        binding.totalQuestionsText.text = totalQuestions.toString()
        binding.answeredQuestionsText.text = answeredQuestions.toString()
        binding.scoreText.text = "$score/$totalQuestions"
        binding.diagnosisText.text = diagnosis

        Log.d("QuizHistoryDetail", "Displaying ${recommendations.size} recommendations")
        displayRecommendations(recommendations)
    }

    private fun displayRecommendations(recommendations: List<String>) {
        binding.recommendationsLayout.removeAllViews()

        if (recommendations.isEmpty()) {
            Log.w("QuizHistoryDetail", "No recommendations to display")
            // Optionally add a "No recommendations" message
            val noRecView = TextView(this).apply {
                text = "Tidak ada rekomendasi tersedia"
                textSize = 14f
                setTextColor(ContextCompat.getColor(this@QuizHistoryDetailActivity, R.color.md_theme_onSurface))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = dpToPx(12)
                }
            }
            binding.recommendationsLayout.addView(noRecView)
            return
        }

        recommendations.forEachIndexed { index, recommendation ->
            if (recommendation.isNotBlank()) {
                val recommendationView = createRecommendationView(index + 1, recommendation)
                binding.recommendationsLayout.addView(recommendationView)
            }
        }
    }

    private fun createRecommendationView(number: Int, text: String): LinearLayout {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(12)
            }
        }

        val numberView = TextView(this).apply {
            setText(number.toString())
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@QuizHistoryDetailActivity, R.color.md_theme_onPrimary))
            background = ContextCompat.getDrawable(this@QuizHistoryDetailActivity, R.drawable.circle_background)
            backgroundTintList = ContextCompat.getColorStateList(this@QuizHistoryDetailActivity, R.color.md_theme_primary)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)).apply {
                marginEnd = dpToPx(12)
                topMargin = dpToPx(2)
            }
        }

        val textView = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@QuizHistoryDetailActivity, R.color.md_theme_onSurface))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setLineSpacing(dpToPx(2).toFloat(), 1.0f)
        }

        layout.addView(numberView)
        layout.addView(textView)
        return layout
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
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
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)
    }
}