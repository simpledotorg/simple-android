package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class MedicalHistorySummaryUpdate : Update<MedicalHistorySummaryModel, MedicalHistorySummaryEvent, MedicalHistorySummaryEffect> {

  override fun update(
      model: MedicalHistorySummaryModel,
      event: MedicalHistorySummaryEvent
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    return when (event) {
      is MedicalHistoryLoaded -> medicalHistoryLoaded(model, event)
      is SummaryMedicalHistoryAnswerToggled -> medicalHistoryAnswerToggled(model, event)
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
      is SuspectedOptionVisibilityDetermined -> next(
          model.diagnosisSuspectedOptionVisibilityLoaded(
              event.showHypertensionSuspectedOption,
              event.showDiabetesSuspectedOption
          )
      )
    }
  }

  private fun medicalHistoryLoaded(
      model: MedicalHistorySummaryModel,
      event: MedicalHistoryLoaded
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    val updatedModel = model.medicalHistoryLoaded(event.medicalHistory)

    return if (updatedModel.hasDeterminedSuspectedOptionVisibility) {
      next(updatedModel)
    } else {
      next(updatedModel, DetermineSuspectedOptionVisibility(event.medicalHistory))
    }
  }

  private fun medicalHistoryAnswerToggled(
      model: MedicalHistorySummaryModel,
      event: SummaryMedicalHistoryAnswerToggled
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    val updatedModel = model.answerToggled(event.question, event.answer)
    return next(updatedModel, SaveUpdatedMedicalHistory(updatedModel.medicalHistory!!))
  }
}
