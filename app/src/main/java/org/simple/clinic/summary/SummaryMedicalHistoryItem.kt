package org.simple.clinic.summary

import android.view.View
import android.widget.TextView
import com.xwray.groupie.ViewHolder
import io.reactivex.subjects.Subject
import kotterknife.bindView
import org.simple.clinic.R
import org.simple.clinic.medicalhistory.MedicalHistory
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

    holder.heartAttackQuestionView.apply {
      render(HAS_HAD_A_HEART_ATTACK)
      checkBox.isChecked = medicalHistory.hasHadHeartAttack
    }

    holder.strokeQuestionView.apply {
      render(HAS_HAD_A_STROKE)
      checkBox.isChecked = medicalHistory.hasHadStroke
    }

    holder.kidneyDiseaseQuestionView.apply {
      render(HAS_HAD_A_KIDNEY_DISEASE)
      checkBox.isChecked = medicalHistory.hasHadKidneyDisease
    }

    holder.hypertensionQuestionView.apply {
      render(IS_ON_TREATMENT_FOR_HYPERTENSION)
      checkBox.isChecked = medicalHistory.isOnTreatmentForHypertension
    }

    holder.diabetesQuestionView.apply {
      render(HAS_DIABETES)
      checkBox.isChecked = medicalHistory.hasDiabetes
    }
  }

  class HistoryViewHolder(rootView: View) : ViewHolder(rootView) {
    val lastUpdatedAtTextView by bindView<TextView>(R.id.patientsummary_medicalhistory_last_update_timestamp)
    val heartAttackQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_heartattack)
    val strokeQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_stroke)
    val kidneyDiseaseQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_kidney)
    val hypertensionQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_hypertension)
    val diabetesQuestionView by bindView<MedicalHistoryQuestionView>(R.id.patientsummary_medicalhistory_question_diabetes)

    init {
      diabetesQuestionView.divider.visibility = View.GONE
    }
  }
}
