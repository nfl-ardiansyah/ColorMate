package com.fchrl.colormate

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.fchrl.colormate.databinding.ItemQuizHistoryBinding

class QuizHistoryAdapter(
    private val historyList: List<QuizHistoryItem>
) : RecyclerView.Adapter<QuizHistoryAdapter.HistoryViewHolder>() {

    inner class HistoryViewHolder(private val binding: ItemQuizHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: QuizHistoryItem) {
            binding.apply {
                // Set basic info
                scoreText.text = "${item.score}/${item.getScoredQuestionCount()}"
                percentageText.text = "${item.getScorePercentage()}%"
                diagnosisText.text = item.diagnosis
                dateText.text = item.getFormattedDate()
                answeredQuestionsText.text = "${item.answeredQuestions} dari ${item.totalQuestions} pertanyaan"

                // Set score-based colors and progress
                val scoreColor = ContextCompat.getColor(itemView.context, item.getScoreBasedColorAlternative())
                scoreText.setTextColor(scoreColor)
                percentageText.setTextColor(scoreColor)

                // Set progress bar
                val progress = item.getScorePercentage()
                progressBar.progress = progress
                progressBar.progressTintList = ContextCompat.getColorStateList(
                    itemView.context,
                    item.getScoreBasedColorAlternative()
                )

                // Set diagnosis-based icon and color (for medical indication)
                val diagnosisIcon = item.getScoreBasedIcon()
                val diagnosisColor = item.getScoreBasedColorAlternative()

                diagnosisIconView.setImageResource(diagnosisIcon)
                diagnosisIconView.imageTintList = ContextCompat.getColorStateList(
                    itemView.context,
                    diagnosisColor
                )

                // Optional: Add risk level indicator
                val riskLevel = item.getColorBlindnessRiskLevel()
                val riskDescription = item.getScoreLevelDescription()

                // You can add a risk indicator text view if needed
                // riskIndicatorText.text = riskDescription
                // riskIndicatorText.setTextColor(ContextCompat.getColor(itemView.context, diagnosisColor))

                // Set click listener for navigation to detail
                root.setOnClickListener {
                    val context = itemView.context
                    val intent = Intent(context, QuizHistoryDetailActivity::class.java).apply {
                        putExtra("history_id", item.id)
                        putExtra("total_questions", item.totalQuestions)
                        putExtra("answered_questions", item.answeredQuestions)
                        putExtra("score", item.score)
                        putExtra("diagnosis", item.diagnosis)
                        putExtra("created_at", item.createdAt.time)

                        // Add additional detailed data for better analysis
                        putExtra("deficiency_type", item.deficiencyType)
                        putExtra("severity_level", item.severityLevel)
                        putExtra("confidence_level", item.confidenceLevel)
                        putExtra("protanopia_score", item.protanopiaScore)
                        putExtra("deuteranopia_score", item.deuteranopiaScore)
                        putExtra("tritanopia_score", item.tritanopiaScore)
                        putExtra("adaptive_score", item.adaptiveScore)
                        putExtra("impact_score", item.impactScore)
                        putExtra("recommendations", item.recommendations.toTypedArray())
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemQuizHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(historyList[position])
    }

    override fun getItemCount(): Int = historyList.size
}