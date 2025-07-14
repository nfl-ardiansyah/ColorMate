package com.fchrl.colormate

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fchrl.colormate.databinding.ActivityQuizHistoryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class QuizHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuizHistoryBinding
    private lateinit var historyAdapter: QuizHistoryAdapter
    private val historyList = mutableListOf<QuizHistoryItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuizHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupRecyclerView()
        loadQuizHistory()
        StatusBarHelper.setupStatusBar(this)
        setBackgroundBasedOnTheme()
    }

    private fun setupClickListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = QuizHistoryAdapter(historyList)
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@QuizHistoryActivity)
            adapter = historyAdapter
        }
    }

    private fun loadQuizHistory() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            showError("User not logged in")
            return
        }

        // Show loading
        binding.progressBar.visibility = View.VISIBLE
        binding.emptyStateLayout.visibility = View.GONE
        binding.historyRecyclerView.visibility = View.GONE

        val db = FirebaseFirestore.getInstance()
        db.collection("users")
            .document(user.uid)
            .collection("history")
            .orderBy("created_at", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                historyList.clear()

                for (document in documents) {
                    val totalQuestions = document.getLong("total_questions")?.toInt() ?: 0
                    val answeredQuestions = document.getLong("answered_questions")?.toInt() ?: 0
                    val score = document.getLong("score")?.toInt() ?: 0
                    val diagnosis = document.getString("diagnosis") ?: ""
                    val createdAt = document.getTimestamp("created_at")

                    // Ambil recommendations jika ada
                    val recommendationsList = document.get("recommendations") as? List<String> ?: emptyList()

                    val historyItem = QuizHistoryItem(
                        id = document.id,
                        totalQuestions = totalQuestions,
                        answeredQuestions = answeredQuestions,
                        score = score,
                        diagnosis = diagnosis,
                        createdAt = createdAt?.toDate() ?: Date(),
                        recommendations = recommendationsList
                    )

                    historyList.add(historyItem)
                }

                // Hide loading
                binding.progressBar.visibility = View.GONE

                if (historyList.isEmpty()) {
                    // Show empty state
                    binding.emptyStateLayout.visibility = View.VISIBLE
                    binding.historyRecyclerView.visibility = View.GONE
                } else {
                    // Show history list
                    binding.emptyStateLayout.visibility = View.GONE
                    binding.historyRecyclerView.visibility = View.VISIBLE
                    historyAdapter.notifyDataSetChanged()
                }

                Log.d("QuizHistoryActivity", "Loaded ${historyList.size} history items")
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                binding.emptyStateLayout.visibility = View.VISIBLE
                binding.historyRecyclerView.visibility = View.GONE
                Log.e("QuizHistoryActivity", "Error loading quiz history", e)
                showError("Gagal memuat riwayat kuis")
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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