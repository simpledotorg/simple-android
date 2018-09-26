package org.simple.clinic.medicalhistory

import android.support.annotation.StringRes
import org.simple.clinic.R

enum class MedicalHistoryQuestion(@StringRes val questionRes: Int) {
  HAS_HAD_A_HEART_ATTACK(R.string.medicalhistory_question_heartattack),
  HAS_HAD_A_STROKE(R.string.medicalhistory_question_stroke),
  HAS_HAD_A_KIDNEY_DISEASE(R.string.medicalhistory_question_kidney),
  IS_ON_TREATMENT_FOR_HYPERTENSION(R.string.medicalhistory_question_hypertension),
  HAS_DIABETES(R.string.medicalhistory_question_diabetes),
  NONE(R.string.medicalhistory_question_none)
}
