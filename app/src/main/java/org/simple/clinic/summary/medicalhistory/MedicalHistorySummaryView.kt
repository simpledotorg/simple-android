package org.simple.clinic.summary.medicalhistory

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.medicalhistory_summary_view.view.*
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestionView
import org.simple.clinic.util.RelativeTimestamp
import org.threeten.bp.format.DateTimeFormatter

private typealias AnswerToggled = (MedicalHistoryQuestion, Answer) -> Unit

class MedicalHistorySummaryView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.medicalhistory_summary_view, this, true)
    diabetesQuestionView.hideDivider()
  }

  var answerToggled: AnswerToggled? = null

  fun bind(
      medicalHistory: MedicalHistory,
      lastUpdatedAt: RelativeTimestamp,
      dateFormatter: DateTimeFormatter
  ) {
    val updatedAtDisplayText = lastUpdatedAt.displayText(context, dateFormatter)

    lastUpdatedAtTextView.text = context.getString(
        R.string.patientsummary_medicalhistory_last_updated,
        updatedAtDisplayText
    )

    diagnosedForHypertensionQuestionView.render(DIAGNOSED_WITH_HYPERTENSION, medicalHistory.diagnosedWithHypertension, answerToggled)
    treatmentForHypertensionQuestionView.render(IS_ON_TREATMENT_FOR_HYPERTENSION, medicalHistory.isOnTreatmentForHypertension, answerToggled)
    heartAttackQuestionView.render(HAS_HAD_A_HEART_ATTACK, medicalHistory.hasHadHeartAttack, answerToggled)
    strokeQuestionView.render(HAS_HAD_A_STROKE, medicalHistory.hasHadStroke, answerToggled)
    kidneyDiseaseQuestionView.render(HAS_HAD_A_KIDNEY_DISEASE, medicalHistory.hasHadKidneyDisease, answerToggled)
    diabetesQuestionView.render(HAS_DIABETES, medicalHistory.hasDiabetes, answerToggled)
  }

  private fun MedicalHistoryQuestionView.render(
      question: MedicalHistoryQuestion,
      answer: Answer,
      answerToggled: AnswerToggled?
  ) {
    render(question, answer)
    answerChangeListener = { newAnswer -> answerToggled?.invoke(question, newAnswer) }
  }
}
