package org.simple.clinic.medicalhistory

import androidx.annotation.StringRes
import org.simple.clinic.R
import org.simple.clinic.appconfig.Country

sealed class MedicalHistoryQuestion(
    @StringRes val questionRes: Int
) {
  object DiagnosedWithHypertension : MedicalHistoryQuestion(R.string.medicalhistory_diagnosis_hypertension)
  object HasHadAHeartAttack : MedicalHistoryQuestion(R.string.medicalhistory_question_heartattack)
  object HasHadAStroke : MedicalHistoryQuestion(R.string.medicalhistory_question_stroke)
  object HasHadAKidneyDisease : MedicalHistoryQuestion(R.string.medicalhistory_question_kidney)
  object DiagnosedWithDiabetes : MedicalHistoryQuestion(R.string.medicalhistory_diagnosis_diabetes)
  data class IsOnHypertensionTreatment(
      private val isoCountryCode: String
  ) : MedicalHistoryQuestion(
      questionRes = when (isoCountryCode) {
        Country.SRI_LANKA -> R.string.medicalhistory_question_is_on_hypertension_treatment_sri_lanka
        else -> R.string.medicalhistory_question_is_on_hypertension_treatment
      }
  )

  object IsOnDiabetesTreatment : MedicalHistoryQuestion(R.string.medicalhistory_question_is_on_diabetes_treatment)
}
