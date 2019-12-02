package org.simple.clinic.summary.medicalhistory

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.cardview.widget.CardView
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.patientsummary_medicalhistoryview_content.view.*
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.Answer
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestionView
import org.simple.clinic.summary.SummaryMedicalHistoryAnswerToggled
import org.simple.clinic.util.RelativeTimestamp
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.format.DateTimeFormatter

class MedicalHistorySummaryView(
    context: Context,
    attributeSet: AttributeSet
) : CardView(context, attributeSet) {

  init {
    LayoutInflater.from(context).inflate(R.layout.patientsummary_medicalhistoryview_content, this, true)
    diabetesQuestionView.hideDivider()
  }

  fun bind(
      medicalHistory: MedicalHistory,
      lastUpdatedAt: RelativeTimestamp,
      dateFormatter: DateTimeFormatter,
      uiEvents: Subject<UiEvent>
  ) {
    val updatedAtDisplayText = lastUpdatedAt.displayText(context, dateFormatter)

    lastUpdatedAtTextView.text = context.getString(
        R.string.patientsummary_medicalhistory_last_updated,
        updatedAtDisplayText
    )

    val renderQuestionView = { view: MedicalHistoryQuestionView, question: MedicalHistoryQuestion, answer: Answer ->
      view.render(question, answer)
      view.answerChangeListener = { newAnswer ->
        uiEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, newAnswer))
      }
    }

    run {
      renderQuestionView(diagnosedForHypertensionQuestionView, MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION, medicalHistory.diagnosedWithHypertension)
      renderQuestionView(treatmentForHypertensionQuestionView, MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION, medicalHistory.isOnTreatmentForHypertension)
      renderQuestionView(heartAttackQuestionView, MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK, medicalHistory.hasHadHeartAttack)
      renderQuestionView(strokeQuestionView, MedicalHistoryQuestion.HAS_HAD_A_STROKE, medicalHistory.hasHadStroke)
      renderQuestionView(kidneyDiseaseQuestionView, MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE, medicalHistory.hasHadKidneyDisease)
      renderQuestionView(diabetesQuestionView, MedicalHistoryQuestion.HAS_DIABETES, medicalHistory.hasDiabetes)
    }
  }
}
