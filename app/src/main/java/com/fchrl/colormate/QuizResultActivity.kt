package com.fchrl.colormate

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fchrl.colormate.databinding.ActivityQuizResultBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QuizResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        displayResults()
        StatusBarHelper.setupStatusBar(this)
        setBackgroundBasedOnTheme()
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.retakeQuizBtn.setOnClickListener {
            val intent = Intent(this, ColorBlindQuizActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        binding.backToHomeBtn.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun displayResults() {
        val totalQuestions = intent.getIntExtra("total_questions", 0)
        val answeredQuestions = intent.getIntExtra("answered_questions", 0)
        val score = intent.getIntExtra("score", 0)
        val diagnosis = intent.getStringExtra("diagnosis") ?: ""
        val recommendations = intent.getStringArrayExtra("recommendations") ?: arrayOf()

        binding.totalQuestionsText.text = totalQuestions.toString()
        binding.answeredQuestionsText.text = answeredQuestions.toString()
        binding.scoreText.text = "$score/$totalQuestions"
        binding.diagnosisText.text = diagnosis

        displayRecommendations(recommendations.toList())

        // Simpan ke Firestore dengan recommendations
        saveQuizHistoryToFirestore(
            totalQuestions,
            answeredQuestions,
            score,
            diagnosis,
            recommendations.toList()
        )
    }

    private fun displayRecommendations(recommendations: List<String>) {
        binding.recommendationsLayout.removeAllViews()

        recommendations.forEachIndexed { index, recommendation ->
            val recommendationView = createRecommendationView(index + 1, recommendation)
            binding.recommendationsLayout.addView(recommendationView)
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
            setTextColor(ContextCompat.getColor(this@QuizResultActivity, R.color.md_theme_onPrimary))
            background = ContextCompat.getDrawable(this@QuizResultActivity, R.drawable.circle_background)
            backgroundTintList = ContextCompat.getColorStateList(this@QuizResultActivity, R.color.md_theme_primary)
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(24), dpToPx(24)).apply {
                marginEnd = dpToPx(12)
                topMargin = dpToPx(2)
            }
        }

        val textView = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@QuizResultActivity, R.color.md_theme_onSurface))
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

    private fun saveQuizHistoryToFirestore(
        totalQuestions: Int,
        answeredQuestions: Int,
        score: Int,
        diagnosis: String,
        recommendations: List<String>
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("QuizResultActivity", "User not logged in, skipping Firestore save")
            return
        }

        val historyData = hashMapOf(
            "total_questions" to totalQuestions,
            "answered_questions" to answeredQuestions,
            "score" to score,
            "diagnosis" to diagnosis,
            "recommendations" to recommendations, // Simpan recommendations ke Firestore
            "created_at" to Timestamp.now()
        )

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(user.uid)
            .collection("history")
            .add(historyData)
            .addOnSuccessListener {
                Log.d("QuizResultActivity", "Quiz history saved successfully to Firestore")
            }
            .addOnFailureListener { e ->
                Log.e("QuizResultActivity", "Error saving quiz history", e)
            }
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