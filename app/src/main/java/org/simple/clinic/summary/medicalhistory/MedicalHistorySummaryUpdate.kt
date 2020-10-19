package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Update
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.mobius.next

class MedicalHistorySummaryUpdate : Update<MedicalHistorySummaryModel, MedicalHistorySummaryEvent, MedicalHistorySummaryEffect> {

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

    val updatedMedicalHistory = savedMedicalHistory.answered(toggledQuestion, event.answer)
    effects.add(SaveUpdatedMedicalHistory(updatedMedicalHistory))

    return dispatch(effects)
  }
}
