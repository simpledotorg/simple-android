package org.simple.clinic.medicalhistory

import android.support.annotation.StringRes
import org.simple.clinic.R

enum class MedicalHistoryQuestion(
    @StringRes val questionRes: Int,
    val setter: (OngoingMedicalHistoryEntry, Boolean) -> OngoingMedicalHistoryEntry
) {

  HAS_HAD_A_HEART_ATTACK(
      R.string.medicalhistory_question_heartattack,
      setter = { ongoingEntry, selected ->
        ongoingEntry.copy(hasHadHeartAttack = selected)
      }),

  HAS_HAD_A_STROKE(
      R.string.medicalhistory_question_stroke,
      setter = { ongoingEntry, selected ->
        ongoingEntry.copy(hasHadStroke = selected)
      }),

  HAS_HAD_A_KIDNEY_DISEASE(
      R.string.medicalhistory_question_kidney,
      setter = { ongoingEntry, selected ->
        ongoingEntry.copy(hasHadKidneyDisease = selected)
      }),

  IS_ON_TREATMENT_FOR_HYPERTENSION(
      R.string.medicalhistory_question_hypertension,
      setter = { ongoingEntry, selected ->
        ongoingEntry.copy(isOnTreatmentForHypertension = selected)
      }),

  HAS_DIABETES(
      R.string.medicalhistory_question_diabetes,
      setter = { ongoingEntry, selected ->
        ongoingEntry.copy(hasDiabetes = selected)
      }),

  NONE(
      R.string.medicalhistory_question_none,
      setter = { ongoingEntry, _ -> ongoingEntry });
}
