package org.simple.clinic.medicalhistory

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION

@Parcelize
data class OngoingMedicalHistoryEntry(
    val hasHadHeartAttack: Answer = Unanswered,
    val hasHadStroke: Answer = Unanswered,
    val hasHadKidneyDisease: Answer = Unanswered,
    val diagnosedWithHypertension: Answer = Unanswered,
    val isOnTreatmentForHypertension: Answer = Unanswered,
    val hasDiabetes: Answer = Unanswered
) : Parcelable {

  fun answerChanged(question: MedicalHistoryQuestion, answer: Answer): OngoingMedicalHistoryEntry {
    return when (question) {
      DIAGNOSED_WITH_HYPERTENSION -> copy(diagnosedWithHypertension = answer)
      IS_ON_TREATMENT_FOR_HYPERTENSION -> copy(isOnTreatmentForHypertension = answer)
      HAS_HAD_A_HEART_ATTACK -> copy(hasHadHeartAttack = answer)
      HAS_HAD_A_STROKE -> copy(hasHadStroke = answer)
      HAS_HAD_A_KIDNEY_DISEASE -> copy(hasHadKidneyDisease = answer)
      HAS_DIABETES -> copy(hasDiabetes = answer)
    }
  }
}
