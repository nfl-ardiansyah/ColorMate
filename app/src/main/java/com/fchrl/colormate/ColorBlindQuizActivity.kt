package com.fchrl.colormate

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.fchrl.colormate.databinding.ActivityColorBlindQuizBinding

class ColorBlindQuizActivity : AppCompatActivity() {
    private lateinit var binding: ActivityColorBlindQuizBinding
    private lateinit var questions: List<QuizQuestion>
    private lateinit var shuffledQuestions: List<ShuffledQuestion> // Tambah ini
    private var currentQuestionIndex = 0
    private val userAnswers = mutableListOf<UserAnswer>()
    private val TAG = "ColorBlindQuiz"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityColorBlindQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeQuiz()
        setupClickListeners()
        setupBackPressHandler()
        StatusBarHelper.setupStatusBar(this)
        setBackgroundBasedOnTheme()
    }

    private fun initializeQuiz() {
        try {
            questions = QuizDataProvider.getQuizQuestions()

            // Acak pertanyaan dan pilihan jawabannya
            shuffledQuestions = questions.map { question ->
                val shuffledOptions = question.options.shuffled()
                val correctAnswerIndex = if (question.correctAnswer != null) {
                    shuffledOptions.indexOfFirst { it.id == question.correctAnswer }
                } else null

                ShuffledQuestion(
                    originalQuestion = question,
                    shuffledOptions = shuffledOptions,
                    correctAnswerIndex = correctAnswerIndex
                )
            }.shuffled() // Acak urutan pertanyaan juga

            Log.d(TAG, "Total questions loaded: ${shuffledQuestions.size}")

            if (shuffledQuestions.isEmpty()) {
                Log.e(TAG, "No questions loaded!")
                Toast.makeText(this, "Gagal memuat pertanyaan kuis", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            displayCurrentQuestion()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing quiz: ${e.message}")
            Toast.makeText(this, "Terjadi kesalahan saat memuat kuis", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backBtn.setOnClickListener {
            showExitConfirmationDialog()
        }

        // Previous button
        binding.previousBtn.setOnClickListener {
            Log.d(TAG, "Previous button clicked")
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                displayCurrentQuestion()
            }
        }

        // Next button
        binding.nextBtn.setOnClickListener {
            Log.d(TAG, "Next button clicked")
            if (isAnswerSelected()) {
                Log.d(TAG, "Saving answer and proceeding")
                saveCurrentAnswer()

                if (currentQuestionIndex < shuffledQuestions.size - 1) {
                    currentQuestionIndex++
                    displayCurrentQuestion()
                } else {
                    Log.d(TAG, "Quiz completed, showing results")
                    showQuizResult()
                }
            } else {
                Log.d(TAG, "No answer selected")
                Toast.makeText(this, "Pilih salah satu jawaban terlebih dahulu", Toast.LENGTH_SHORT).show()
            }
        }

        // Option cards click listeners - ubah untuk menggunakan index
        binding.optionACard.setOnClickListener { selectOption(0) }
        binding.optionBCard.setOnClickListener { selectOption(1) }
        binding.optionCCard.setOnClickListener { selectOption(2) }
        binding.optionDCard.setOnClickListener { selectOption(3) }

        // Radio buttons click listeners - ubah untuk menggunakan index
        binding.optionARadio.setOnClickListener { selectOption(0) }
        binding.optionBRadio.setOnClickListener { selectOption(1) }
        binding.optionCRadio.setOnClickListener { selectOption(2) }
        binding.optionDRadio.setOnClickListener { selectOption(3) }
    }

    private fun displayCurrentQuestion() {
        try {
            if (currentQuestionIndex >= shuffledQuestions.size) {
                Log.e(TAG, "Current question index out of bounds: $currentQuestionIndex")
                return
            }

            val shuffledQuestion = shuffledQuestions[currentQuestionIndex]
            val question = shuffledQuestion.originalQuestion
            Log.d(TAG, "Displaying question ${currentQuestionIndex + 1}: ${question.question}")

            // Update progress
            binding.progressText.text = "Pertanyaan ${currentQuestionIndex + 1} dari ${shuffledQuestions.size}"
            binding.progressBar.progress = ((currentQuestionIndex + 1) * 100) / shuffledQuestions.size

            // Update content
            binding.categoryChip.text = question.category
            binding.questionText.text = question.question

            // Show/hide image
            if (question.imageResource != null) {
                binding.questionImage.setImageResource(question.imageResource)
                binding.questionImage.visibility = View.VISIBLE
            } else {
                binding.questionImage.visibility = View.GONE
            }

            // Update options - tanpa menggunakan ABCD, langsung tampilkan teks
            val options = shuffledQuestion.shuffledOptions
            if (options.size >= 4) {
                binding.optionAText.text = options[0].text
                binding.optionBText.text = options[1].text
                binding.optionCText.text = options[2].text
                binding.optionDText.text = options[3].text
            } else {
                Log.e(TAG, "Question doesn't have enough options: ${options.size}")
            }

            // Clear previous selection
            clearSelection()

            // Restore previous answer if exists
            restorePreviousAnswer()

            // Update navigation buttons
            updateNavigationButtons()
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying question: ${e.message}")
        }
    }

    private fun selectOption(optionIndex: Int) {
        Log.d(TAG, "Selecting option index: $optionIndex")

        // Clear all selections first
        clearSelection()

        // Highlight selected option dan set radio button
        when (optionIndex) {
            0 -> {
                highlightSelectedCard(binding.optionACard)
                binding.optionARadio.isChecked = true
                binding.optionsRadioGroup.check(R.id.optionARadio)
            }
            1 -> {
                highlightSelectedCard(binding.optionBCard)
                binding.optionBRadio.isChecked = true
                binding.optionsRadioGroup.check(R.id.optionBRadio)
            }
            2 -> {
                highlightSelectedCard(binding.optionCCard)
                binding.optionCRadio.isChecked = true
                binding.optionsRadioGroup.check(R.id.optionCRadio)
            }
            3 -> {
                highlightSelectedCard(binding.optionDCard)
                binding.optionDRadio.isChecked = true
                binding.optionsRadioGroup.check(R.id.optionDRadio)
            }
        }

        Log.d(TAG, "After selection - RadioGroup checked ID: ${binding.optionsRadioGroup.checkedRadioButtonId}")
        Log.d(TAG, "Is answer selected after selection: ${isAnswerSelected()}")

        // Update navigation buttons
        updateNavigationButtons()
    }

    private fun clearSelection() {
        Log.d(TAG, "Clearing all selections")

        // Clear radio buttons
        binding.optionsRadioGroup.clearCheck()

        // Uncheck individual radio buttons
        binding.optionARadio.isChecked = false
        binding.optionBRadio.isChecked = false
        binding.optionCRadio.isChecked = false
        binding.optionDRadio.isChecked = false

        // Reset card styles
        resetCardStyle(binding.optionACard)
        resetCardStyle(binding.optionBCard)
        resetCardStyle(binding.optionCCard)
        resetCardStyle(binding.optionDCard)
    }

    private fun highlightSelectedCard(card: com.google.android.material.card.MaterialCardView) {
        try {
            card.strokeColor = ContextCompat.getColor(this, R.color.md_theme_primary)
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.md_theme_primaryContainer))
        } catch (e: Exception) {
            Log.e(TAG, "Error highlighting card: ${e.message}")
        }
    }

    private fun resetCardStyle(card: com.google.android.material.card.MaterialCardView) {
        try {
            card.strokeColor = ContextCompat.getColor(this, R.color.md_theme_outline)
            card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.md_theme_surface))
        } catch (e: Exception) {
            Log.e(TAG, "Error resetting card style: ${e.message}")
        }
    }

    private fun isAnswerSelected(): Boolean {
        val isSelected = binding.optionsRadioGroup.checkedRadioButtonId != -1
        Log.d(TAG, "isAnswerSelected: $isSelected, checkedRadioButtonId: ${binding.optionsRadioGroup.checkedRadioButtonId}")
        return isSelected
    }

    private fun saveCurrentAnswer() {
        try {
            val selectedOptionIndex = when (binding.optionsRadioGroup.checkedRadioButtonId) {
                R.id.optionARadio -> 0
                R.id.optionBRadio -> 1
                R.id.optionCRadio -> 2
                R.id.optionDRadio -> 3
                else -> {
                    Log.e(TAG, "No valid option selected for saving")
                    return
                }
            }

            Log.d(TAG, "Saving answer: option index $selectedOptionIndex for question ${currentQuestionIndex + 1}")

            val shuffledQuestion = shuffledQuestions[currentQuestionIndex]
            val isCorrect = if (shuffledQuestion.correctAnswerIndex != null) {
                selectedOptionIndex == shuffledQuestion.correctAnswerIndex
            } else {
                null // Untuk pertanyaan reflektif
            }

            // Remove existing answer for this question if any
            userAnswers.removeAll { it.questionId == shuffledQuestion.originalQuestion.id }

            // Add new answer
            userAnswers.add(
                UserAnswer(
                    questionId = shuffledQuestion.originalQuestion.id,
                    selectedOptionIndex = selectedOptionIndex,
                    isCorrect = isCorrect
                )
            )

            Log.d(TAG, "Answer saved. Total answers: ${userAnswers.size}")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving answer: ${e.message}")
        }
    }

    private fun restorePreviousAnswer() {
        try {
            val shuffledQuestion = shuffledQuestions[currentQuestionIndex]
            val previousAnswer = userAnswers.find { it.questionId == shuffledQuestion.originalQuestion.id }

            previousAnswer?.let { answer ->
                Log.d(TAG, "Restoring previous answer: option index ${answer.selectedOptionIndex}")
                selectOption(answer.selectedOptionIndex)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error restoring previous answer: ${e.message}")
        }
    }

    private fun updateNavigationButtons() {
        try {
            // Previous button
            binding.previousBtn.visibility = if (currentQuestionIndex > 0) View.VISIBLE else View.GONE

            // Next button
            val isAnswerSelected = isAnswerSelected()
            binding.nextBtn.isEnabled = isAnswerSelected
            binding.nextBtn.text = if (currentQuestionIndex == shuffledQuestions.size - 1) "Selesai" else "Selanjutnya"

            Log.d(TAG, "Navigation buttons updated - Next enabled: ${binding.nextBtn.isEnabled}, Text: ${binding.nextBtn.text}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating navigation buttons: ${e.message}")
        }
    }

    private fun showQuizResult() {
        try {
            Log.d(TAG, "Calculating quiz results...")
            val result = QuizDataProvider.calculateDiagnosis(userAnswers)

            val intent = Intent(this, QuizResultActivity::class.java).apply {
                putExtra("total_questions", result.totalQuestions)
                putExtra("answered_questions", result.answeredQuestions)
                putExtra("score", result.score)
                putExtra("diagnosis", result.diagnosis)
                putExtra("recommendations", result.recommendations.toTypedArray())
            }

            Log.d(TAG, "Starting QuizResultActivity")
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e(TAG, "Error showing quiz result: ${e.message}")
            Toast.makeText(this, "Terjadi kesalahan saat menampilkan hasil", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showExitConfirmationDialog() {
        try {
            val iconColor = ContextCompat.getColor(this, R.color.md_theme_onBackground)
            val dialog = AlertDialog.Builder(this, R.style.CustomAlertDialog)
                .setTitle("Keluar dari Kuis")
                .setMessage("Apakah Anda yakin ingin keluar? Progres kuis akan hilang.")
                .setIcon(R.drawable.ic_back)
                .setPositiveButton("Ya") { _, _ ->
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
        } catch (e: Exception) {
            Log.e(TAG, "Error showing exit dialog: ${e.message}")
            finish() // Fallback jika dialog gagal
        }
    }

    private fun setupBackPressHandler() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun setBackgroundBasedOnTheme() {
        try {
            val currentMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            val backgroundRes = if (currentMode == Configuration.UI_MODE_NIGHT_YES) {
                R.drawable.app_bg_inv
            } else {
                R.drawable.app_bg
            }
            binding.main.setBackgroundResource(backgroundRes)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting background: ${e.message}")
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setBackgroundBasedOnTheme()
        StatusBarHelper.setupStatusBar(this)
    }
}