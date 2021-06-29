package org.simple.clinic.medicalhistory

import androidx.annotation.StringRes
import org.simple.clinic.R

enum class MedicalHistoryQuestion(@StringRes val questionRes: Int) {
  DIAGNOSED_WITH_HYPERTENSION(R.string.medicalhistory_question_diagnosed_with_hypertension),
  HAS_HAD_A_HEART_ATTACK(R.string.medicalhistory_question_heartattack),
  HAS_HAD_A_STROKE(R.string.medicalhistory_question_stroke),
  HAS_HAD_A_KIDNEY_DISEASE(R.string.medicalhistory_question_kidney),
  DIAGNOSED_WITH_DIABETES(R.string.medicalhistory_question_diabetes),
  IS_ON_HYPERTENSION_TREATMENT(R.string.medicalhistory_question_is_on_hypertension_treatment)
}
