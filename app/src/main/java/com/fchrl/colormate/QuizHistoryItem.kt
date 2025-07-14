package com.fchrl.colormate

import java.text.SimpleDateFormat
import java.util.*

// Data class untuk item history
data class QuizHistoryItem(
    val id: String,
    val totalQuestions: Int,
    val answeredQuestions: Int,
    val score: Int,
    val diagnosis: String,
    val createdAt: Date,
    val recommendations: List<String> = emptyList(),
    // Tambahan field dari QuizResult untuk analisis yang lebih detail
    val deficiencyType: String = "Normal",
    val severityLevel: String = "Normal",
    val confidenceLevel: String = "Sedang",
    val protanopiaScore: Int = 0,
    val deuteranopiaScore: Int = 0,
    val tritanopiaScore: Int = 0,
    val adaptiveScore: Int = 0,
    val impactScore: Int = 0
) {
    // Hitung persentase skor berdasarkan soal yang dijawab
    // Sekarang semua 10 soal memiliki correctAnswer dan bisa di-score
    fun getScorePercentage(): Int {
        return if (answeredQuestions > 0) {
            (score * 100) / answeredQuestions
        } else 0
    }

    // Fungsi helper untuk menghitung jumlah soal yang bisa di-score
    // Sekarang semua 10 soal bisa di-score karena semua memiliki correctAnswer
    fun getScoredQuestionCount(): Int {
        return totalQuestions // Semua soal bisa di-score
    }

    fun getFormattedDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        return sdf.format(createdAt)
    }

    fun getDetailedFormattedDate(): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy\nHH:mm WIB", Locale("id", "ID"))
        return sdf.format(createdAt)
    }

    // Deteksi indikasi buta warna berdasarkan severity level dan deficiency type
    fun hasColorBlindnessIndication(): Boolean {
        return deficiencyType != "Normal" ||
                severityLevel !in listOf("Normal", "") ||
                getTotalDeficiencyScore() >= 2 ||
                diagnosis.lowercase().let { diagnosisLower ->
                    diagnosisLower.contains("kesulitan") ||
                            diagnosisLower.contains("mengalami") ||
                            diagnosisLower.contains("terindikasi") ||
                            diagnosisLower.contains("kemungkinan") ||
                            diagnosisLower.contains("deuteranopia") ||
                            diagnosisLower.contains("protanopia") ||
                            diagnosisLower.contains("tritanopia") ||
                            diagnosisLower.contains("buta warna") ||
                            diagnosisLower.contains("defisiensi") ||
                            diagnosisLower.contains("gangguan")
                }
    }

    // Fungsi untuk mendapatkan total skor defisiensi
    fun getTotalDeficiencyScore(): Int {
        return protanopiaScore + deuteranopiaScore + tritanopiaScore
    }

    // Fungsi untuk menentukan tingkat risiko buta warna berdasarkan logika dari QuizProvider
    fun getColorBlindnessRiskLevel(): ColorBlindnessRisk {
        val totalDeficiency = getTotalDeficiencyScore()
        val scorePercentage = getScorePercentage()

        return when {
            // HIGH_DANGER: Sesuai logika QuizProvider - totalIndicators >= 5 dan realWorldImpact >= 2
            totalDeficiency >= 5 && impactScore >= 2 -> ColorBlindnessRisk.HIGH_DANGER

            // HIGH_DANGER: Atau scorePercentage < 50 (sangat rendah)
            scorePercentage < 50 -> ColorBlindnessRisk.HIGH_DANGER

            // DANGER: totalIndicators >= 3 atau scorePercentage < 60
            totalDeficiency >= 3 || scorePercentage < 60 -> ColorBlindnessRisk.DANGER

            // WARNING: totalIndicators >= 2 atau scorePercentage < 80
            totalDeficiency >= 2 || scorePercentage < 80 -> ColorBlindnessRisk.WARNING

            // NORMAL: scorePercentage >= 80 && totalIndicators <= 1
            scorePercentage >= 80 && totalDeficiency <= 1 -> ColorBlindnessRisk.NORMAL

            // Default WARNING untuk kasus lainnya
            else -> ColorBlindnessRisk.WARNING
        }
    }

    // Fungsi untuk menentukan level skor berdasarkan persentase dan logika QuizProvider
    fun getScoreLevel(): ScoreLevel {
        val percentage = getScorePercentage()
        val totalDeficiency = getTotalDeficiencyScore()

        return when {
            // HIGH: Sesuai logika QuizProvider - skor >= 80% dan totalIndicators <= 1
            percentage >= 80 && totalDeficiency <= 1 -> ScoreLevel.HIGH

            // MEDIUM: Skor >= 60% dan totalIndicators <= 2, atau punya strategi adaptif baik
            percentage >= 60 && (totalDeficiency <= 2 || adaptiveScore >= 2) -> ScoreLevel.MEDIUM

            // LOW: Sisanya
            else -> ScoreLevel.LOW
        }
    }

    // Fungsi untuk mendapatkan warna berdasarkan SKOR (untuk progress bar dan indikator skor)
    fun getScoreBasedColor(): Int {
        return when (getScoreLevel()) {
            ScoreLevel.HIGH -> R.color.score_high_green    // Hijau untuk skor tinggi
            ScoreLevel.MEDIUM -> R.color.score_medium_orange // Oranye untuk skor sedang
            ScoreLevel.LOW -> R.color.score_low_red        // Merah untuk skor rendah
        }
    }

    // Fungsi alternatif menggunakan warna tema yang ada (untuk skor)
    fun getScoreBasedColorAlternative(): Int {
        return when (getScoreLevel()) {
            ScoreLevel.HIGH -> R.color.md_theme_primary    // Hijau untuk skor tinggi
            ScoreLevel.MEDIUM -> R.color.warning_color     // Oranye untuk skor sedang
            ScoreLevel.LOW -> R.color.md_theme_error       // Merah untuk skor rendah
        }
    }

    // Fungsi untuk mendapatkan icon berdasarkan SKOR (untuk indikator skor)
    fun getScoreBasedIcon(): Int {
        return when (getScoreLevel()) {
            ScoreLevel.HIGH -> R.drawable.ic_check_circle
            ScoreLevel.MEDIUM -> R.drawable.ic_warning
            ScoreLevel.LOW -> R.drawable.ic_error
        }
    }

    // Fungsi untuk mendapatkan deskripsi level berdasarkan risk level
    fun getScoreLevelDescription(): String {
        return when (getColorBlindnessRiskLevel()) {
            ColorBlindnessRisk.NORMAL -> "Normal"
            ColorBlindnessRisk.WARNING -> "Perlu Perhatian"
            ColorBlindnessRisk.DANGER -> "Indikasi Buta Warna"
            ColorBlindnessRisk.HIGH_DANGER -> "Buta Warna Signifikan"
        }
    }

    // Fungsi untuk mendapatkan deskripsi detail dengan persentase
    fun getScoreDescription(): String {
        val percentage = getScorePercentage()
        val riskLevel = getColorBlindnessRiskLevel()

        return when (riskLevel) {
            ColorBlindnessRisk.NORMAL -> "Normal ($percentage%)"
            ColorBlindnessRisk.WARNING -> "Perlu Perhatian ($percentage%)"
            ColorBlindnessRisk.DANGER -> "Indikasi Buta Warna ($percentage%)"
            ColorBlindnessRisk.HIGH_DANGER -> "Buta Warna Signifikan ($percentage%)"
        }
    }

    // FUNGSI UNTUK DIAGNOSIS/RISK (warna berdasarkan kondisi buta warna)
    fun getDiagnosisColor(): Int {
        return when (getColorBlindnessRiskLevel()) {
            ColorBlindnessRisk.NORMAL -> R.color.md_theme_primary
            ColorBlindnessRisk.WARNING -> R.color.warning_color
            ColorBlindnessRisk.DANGER -> R.color.md_theme_tertiary
            ColorBlindnessRisk.HIGH_DANGER -> R.color.md_theme_error
        }
    }

    fun getDiagnosisIcon(): Int {
        return when (getColorBlindnessRiskLevel()) {
            ColorBlindnessRisk.NORMAL -> R.drawable.ic_check_circle
            ColorBlindnessRisk.WARNING -> R.drawable.ic_warning
            ColorBlindnessRisk.DANGER -> R.drawable.ic_error
            ColorBlindnessRisk.HIGH_DANGER -> R.drawable.ic_error
        }
    }

    // Backward compatibility - HATI-HATI: Sekarang menggunakan skor, bukan risk level
    fun getScoreColor(): Int = getScoreBasedColorAlternative()

    // Fungsi untuk mendapatkan deskripsi status (sama dengan getScoreLevelDescription)
    fun getRiskDescription(): String = getScoreLevelDescription()

    // Fungsi tambahan untuk mendapatkan jenis defisiensi dominan
    // Sesuai dengan logik di QuizProvider
    fun getPrimaryDeficiencyType(): String {
        return when {
            protanopiaScore >= deuteranopiaScore && protanopiaScore >= tritanopiaScore && protanopiaScore > 0 -> "Protanopia/Protanomaly"
            deuteranopiaScore >= tritanopiaScore && deuteranopiaScore > 0 -> "Deuteranopia/Deuteranomaly"
            tritanopiaScore > 0 -> "Tritanopia/Tritanomaly"
            else -> "Normal"
        }
    }

    // Fungsi untuk mendapatkan skor defisiensi tertinggi
    fun getHighestDeficiencyScore(): Int {
        return maxOf(protanopiaScore, deuteranopiaScore, tritanopiaScore)
    }

    // Fungsi untuk mengecek apakah memiliki strategi adaptif yang baik
    fun hasGoodAdaptiveStrategies(): Boolean {
        return adaptiveScore >= 2
    }

    // Fungsi untuk mengecek dampak pada kehidupan sehari-hari
    fun hasRealWorldImpact(): Boolean {
        return impactScore >= 1
    }

    // Fungsi untuk mengecek dampak signifikan (sesuai QuizProvider)
    fun hasSignificantRealWorldImpact(): Boolean {
        return impactScore >= 2
    }

    // Fungsi untuk mendapatkan confidence level sebagai enum
    fun getConfidenceLevelEnum(): ConfidenceLevel {
        return when (confidenceLevel.lowercase()) {
            "tinggi" -> ConfidenceLevel.HIGH
            "sedang" -> ConfidenceLevel.MEDIUM
            else -> ConfidenceLevel.LOW
        }
    }

    // Fungsi untuk mendapatkan rekomendasi berdasarkan prioritas
    fun getPriorityRecommendations(): List<String> {
        return recommendations.take(3) // Ambil 3 rekomendasi teratas
    }

    // Fungsi untuk mendapatkan summary singkat hasil tes
    fun getResultSummary(): String {
        val riskLevel = getColorBlindnessRiskLevel()
        val deficiencyType = getPrimaryDeficiencyType()
        val scorePercentage = getScorePercentage()

        return when (riskLevel) {
            ColorBlindnessRisk.NORMAL -> "Kemampuan persepsi warna dalam rentang normal ($scorePercentage%)"
            ColorBlindnessRisk.WARNING -> when {
                hasGoodAdaptiveStrategies() -> "Kemampuan cukup baik dengan strategi adaptif yang baik ($scorePercentage%)"
                else -> "Kemungkinan kesulitan ringan dalam membedakan warna tertentu ($scorePercentage%)"
            }
            ColorBlindnessRisk.DANGER -> "Terindikasi mengalami $deficiencyType ($scorePercentage%)"
            ColorBlindnessRisk.HIGH_DANGER -> when {
                hasSignificantRealWorldImpact() -> "Kemungkinan besar mengalami $deficiencyType dengan dampak signifikan ($scorePercentage%)"
                else -> "Kemungkinan besar mengalami $deficiencyType ($scorePercentage%)"
            }
        }
    }

    // Fungsi untuk mendapatkan kategori berdasarkan severity dari QuizProvider
    fun getSeverityCategory(): String {
        val totalDeficiency = getTotalDeficiencyScore()
        return when {
            totalDeficiency >= 5 -> "Signifikan"
            totalDeficiency >= 3 -> "Sedang"
            totalDeficiency >= 2 -> "Ringan"
            else -> "Normal"
        }
    }

    // Fungsi untuk mendapatkan level kepercayaan hasil tes
    fun getTestReliability(): String {
        return when {
            answeredQuestions < 8 -> "Rendah - Jawab semua soal untuk hasil yang lebih akurat"
            confidenceLevel.lowercase() == "tinggi" -> "Tinggi"
            confidenceLevel.lowercase() == "sedang" -> "Sedang"
            else -> "Rendah"
        }
    }

    // Fungsi untuk mengecek apakah tes lengkap
    fun isTestComplete(): Boolean {
        return answeredQuestions >= totalQuestions
    }

    // Fungsi untuk mendapatkan progress tes dalam persentase
    fun getTestProgress(): Int {
        return if (totalQuestions > 0) {
            (answeredQuestions * 100) / totalQuestions
        } else 0
    }
}


// Enum untuk level skor
enum class ScoreLevel {
    HIGH,    // 80% ke atas dan indikator defisiensi <= 1
    MEDIUM,  // 60-79% atau memiliki strategi adaptif baik
    LOW      // Di bawah 60% atau indikator defisiensi tinggi
}

// Enum untuk confidence level
enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW
}