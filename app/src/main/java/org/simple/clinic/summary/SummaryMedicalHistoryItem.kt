package org.simple.clinic.summary

import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestionView
import org.simple.clinic.widgets.UiEvent
import java.util.Locale.ENGLISH

data class SummaryMedicalHistoryItem(
    val medicalHistory: MedicalHistory,
    val lastUpdatedAt: RelativeTimestamp
) : GroupieItemWithUiEvents<SummaryMedicalHistoryItem.HistoryViewHolder>(medicalHistory.uuid.hashCode().toLong()) {

  override lateinit var uiEvents: Subject<UiEvent>

  override fun getLayout() = R.layout.list_patientsummary_medicalhistory

  override fun createViewHolder(itemView: View): HistoryViewHolder {
    return HistoryViewHolder(itemView)
  }

  override fun bind(holder: HistoryViewHolder, position: Int) {
    val context = holder.itemView.context
    holder.lastUpdatedAtTextView.text = context.getString(
        R.string.patientsummary_medicalhistory_last_updated,
        lastUpdatedAt.displayText(context).toLowerCase(ENGLISH))

    val renderQuestionView = { view: MedicalHistoryQuestionView, question: MedicalHistoryQuestion, answer: MedicalHistory.Answer ->
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

  class HistoryViewHolder(rootView: View) : ViewHolder(rootView) {
    val lastUpdatedAtTextView by bindView<TextView>(R.id.patientsummary_medicalhistory_last_update_timestamp)
    val diagnosedForHypertensionQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_diagnosed_for_hypertension)
    val treatmentForHypertensionQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_treatment_for_hypertension)
    val heartAttackQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_heartattack)
    val strokeQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_stroke)
    val kidneyDiseaseQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_kidney)
    val diabetesQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_diabetes)

    init {
      diabetesQuestionView.hideDivider()
    }
  }
}
