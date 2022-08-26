package org.simple.clinic.medicalhistory

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.medicalhistory.Answer.Unanswered
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithDiabetes
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DiagnosedWithHypertension
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAHeartAttack
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAKidneyDisease
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HasHadAStroke
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnDiabetesTreatment
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IsOnHypertensionTreatment

@Parcelize
data class OngoingMedicalHistoryEntry(
    val hasHadHeartAttack: Answer = Unanswered,
    val hasHadStroke: Answer = Unanswered,
    val hasHadKidneyDisease: Answer = Unanswered,
    val diagnosedWithHypertension: Answer = Unanswered,
    val isOnHypertensionTreatment: Answer = Unanswered,
    val isOnDiabetesTreatment: Answer = Unanswered,
    val hasDiabetes: Answer = Unanswered
) : Parcelable {

  fun answerChanged(question: MedicalHistoryQuestion, answer: Answer): OngoingMedicalHistoryEntry {
    return when (question) {
      DiagnosedWithHypertension -> copy(diagnosedWithHypertension = answer)
      HasHadAHeartAttack -> copy(hasHadHeartAttack = answer)
      HasHadAStroke -> copy(hasHadStroke = answer)
      HasHadAKidneyDisease -> copy(hasHadKidneyDisease = answer)
      DiagnosedWithDiabetes -> copy(hasDiabetes = answer)
      is IsOnHypertensionTreatment -> copy(isOnHypertensionTreatment = answer)
      IsOnDiabetesTreatment -> copy(isOnDiabetesTreatment = answer)
    }
  }
}
