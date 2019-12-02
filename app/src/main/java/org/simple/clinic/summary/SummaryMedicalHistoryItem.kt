package org.simple.clinic.summary

import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.list_patientsummary_medicalhistory.*
import kotterknife.bindView
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
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.format.DateTimeFormatter

data class SummaryMedicalHistoryItem(
    val medicalHistory: MedicalHistory,
    val lastUpdatedAt: RelativeTimestamp,
    val dateFormatter: DateTimeFormatter
) : GroupieItemWithUiEvents<SummaryMedicalHistoryItem.HistoryViewHolder>(medicalHistory.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_medicalhistory

  override fun createViewHolder(itemView: View): HistoryViewHolder {
    return HistoryViewHolder(itemView)
  }

  override fun bind(holder: HistoryViewHolder, position: Int) {
    val context = holder.itemView.context

    val updatedAtDisplayText = lastUpdatedAt.displayText(context, dateFormatter)

    holder.lastUpdatedAtTextView.text = context.getString(
        R.string.patientsummary_medicalhistory_last_updated,
        updatedAtDisplayText
    )

    val renderQuestionView = { view: MedicalHistoryQuestionView, question: MedicalHistoryQuestion, answer: Answer ->
      view.render(question, answer)
      view.answerChangeListener = { newAnswer ->
        uiEvents.onNext(SummaryMedicalHistoryAnswerToggled(question, newAnswer))
      }
    }

    holder.run {
      renderQuestionView(diagnosedForHypertensionQuestionView, DIAGNOSED_WITH_HYPERTENSION, medicalHistory.diagnosedWithHypertension)
      renderQuestionView(treatmentForHypertensionQuestionView, IS_ON_TREATMENT_FOR_HYPERTENSION, medicalHistory.isOnTreatmentForHypertension)
      renderQuestionView(heartAttackQuestionView, HAS_HAD_A_HEART_ATTACK, medicalHistory.hasHadHeartAttack)
      renderQuestionView(strokeQuestionView, HAS_HAD_A_STROKE, medicalHistory.hasHadStroke)
      renderQuestionView(kidneyDiseaseQuestionView, HAS_HAD_A_KIDNEY_DISEASE, medicalHistory.hasHadKidneyDisease)
      renderQuestionView(diabetesQuestionView, HAS_DIABETES, medicalHistory.hasDiabetes)
    }
  }

  class HistoryViewHolder(override val containerView: View) : ViewHolder(containerView), LayoutContainer {

    init {
      diabetesQuestionView.hideDivider()
    }
  }
}
