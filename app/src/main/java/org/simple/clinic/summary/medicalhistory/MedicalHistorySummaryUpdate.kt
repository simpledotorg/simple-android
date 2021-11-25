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
      is MedicalHistoryLoaded -> next(model.medicalHistoryLoaded(event.medicalHistory))
      is SummaryMedicalHistoryAnswerToggled -> medicalHistoryAnswerToggled(model, event)
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
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
