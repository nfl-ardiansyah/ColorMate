package com.fchrl.colormate

// Data class untuk pilihan jawaban
data class QuizOption(
    val id: String,
    val text: String
)

// Data class untuk pertanyaan
data class QuizQuestion(
    val id: Int,
    val category: String,
    val question: String,
    val options: List<QuizOption>,
    val correctAnswer: String?, // null jika tidak ada jawaban benar/salah
    val explanation: String,
    val type: String,
    val imageResource: Int? = null // untuk pertanyaan dengan gambar
)

// Data class untuk hasil quiz
data class QuizResult(
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val score: Int,
    val diagnosis: String,
    val recommendations: List<String>,
    val deficiencyType: String = "Normal",
    val severityLevel: String = "Normal",
    val confidenceLevel: String = "Sedang",
    val protanopiaScore: Int = 0,
    val deuteranopiaScore: Int = 0,
    val tritanopiaScore: Int = 0,
    val adaptiveScore: Int = 0,
    val impactScore: Int = 0
)

// Data class untuk menyimpan jawaban user
data class ShuffledQuestion(
    val originalQuestion: QuizQuestion,
    val shuffledOptions: List<QuizOption>,
    val correctAnswerIndex: Int? // index dari jawaban benar setelah diacak
)

// Ubah UserAnswer untuk menyimpan index pilihan, bukan huruf
data class UserAnswer(
    val questionId: Int,
    val selectedOptionIndex: Int, // Ubah dari selectedOption: String ke selectedOptionIndex: Int
    val isCorrect: Boolean?
)