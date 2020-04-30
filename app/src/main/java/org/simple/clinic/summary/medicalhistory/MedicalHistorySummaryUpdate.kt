package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Update
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_DIABETES
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion.DIAGNOSED_WITH_HYPERTENSION
import org.simple.clinic.mobius.next

class MedicalHistorySummaryUpdate : Update<MedicalHistorySummaryModel, MedicalHistorySummaryEvent, MedicalHistorySummaryEffect> {

  private val diagnosisQuestions = setOf(DIAGNOSED_WITH_HYPERTENSION, DIAGNOSED_WITH_DIABETES)

  override fun update(
      model: MedicalHistorySummaryModel,
      event: MedicalHistorySummaryEvent
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    return when (event) {
      is MedicalHistoryLoaded -> next(model.medicalHistoryLoaded(event.medicalHistory))
      is SummaryMedicalHistoryAnswerToggled -> medicalHistoryAnswerToggled(event, model.medicalHistory!!)
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
    }
  }

  private fun medicalHistoryAnswerToggled(
      event: SummaryMedicalHistoryAnswerToggled,
      savedMedicalHistory: MedicalHistory
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    val effects = mutableSetOf<MedicalHistorySummaryEffect>()

    val toggledQuestion = event.question
    if (toggledQuestion in diagnosisQuestions) {
      effects.add(HideDiagnosisError)
    }

    val updatedMedicalHistory = savedMedicalHistory.answered(toggledQuestion, event.answer)
    effects.add(SaveUpdatedMedicalHistory(updatedMedicalHistory))

    return dispatch(effects)
  }
}
