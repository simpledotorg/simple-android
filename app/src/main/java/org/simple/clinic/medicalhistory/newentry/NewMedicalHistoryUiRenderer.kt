package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_HEART_ATTACK
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_KIDNEY_DISEASE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.HAS_HAD_A_STROKE
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.IS_ON_TREATMENT_FOR_HYPERTENSION
import org.simple.clinic.mobius.ViewRenderer

class NewMedicalHistoryUiRenderer(
    private val ui: NewMedicalHistoryUi
) : ViewRenderer<NewMedicalHistoryModel> {

  override fun render(model: NewMedicalHistoryModel) {
    if (model.hasLoadedPatientEntry) {
      ui.setPatientName(model.ongoingPatientEntry!!.personalDetails!!.fullName)
    }

    with(model.ongoingMedicalHistoryEntry) {
      ui.renderAnswerForQuestion(HAS_HAD_A_HEART_ATTACK, hasHadHeartAttack)
      ui.renderAnswerForQuestion(HAS_DIABETES, hasDiabetes)
      ui.renderAnswerForQuestion(HAS_HAD_A_KIDNEY_DISEASE, hasHadKidneyDisease)
      ui.renderAnswerForQuestion(HAS_HAD_A_STROKE, hasHadStroke)
      ui.renderAnswerForQuestion(IS_ON_TREATMENT_FOR_HYPERTENSION, isOnTreatmentForHypertension)
      ui.renderAnswerForQuestion(DIAGNOSED_WITH_HYPERTENSION, diagnosedWithHypertension)
    }
  }
}
