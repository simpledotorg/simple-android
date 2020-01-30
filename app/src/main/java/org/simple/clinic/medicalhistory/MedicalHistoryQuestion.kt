package org.simple.clinic.medicalhistory

import androidx.annotation.StringRes
import org.simple.clinic.R

enum class MedicalHistoryQuestion(@StringRes val questionRes: Int) {

  DIAGNOSED_WITH_HYPERTENSION(R.string.medicalhistory_question_diagnosed_with_hypertension),

  // TODO(vs): 2020-01-27 Remove this when migrating to api v4
  @Deprecated(
      message = "this value is no longer in use. Has been left behind for backward compatibility with api v3"
  )
  IS_ON_TREATMENT_FOR_HYPERTENSION(R.string.medicalhistory_question_takes_hypertension_drugs),

  HAS_HAD_A_HEART_ATTACK(R.string.medicalhistory_question_heartattack),
  HAS_HAD_A_STROKE(R.string.medicalhistory_question_stroke),
  HAS_HAD_A_KIDNEY_DISEASE(R.string.medicalhistory_question_kidney),
  DIAGNOSED_WITH_DIABETES(R.string.medicalhistory_question_diabetes),
}
