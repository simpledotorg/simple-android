package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class MedicalHistorySummaryUpdate : Update<MedicalHistorySummaryModel, MedicalHistorySummaryEvent, MedicalHistorySummaryEffect> {

  override fun update(
      model: MedicalHistorySummaryModel,
      event: MedicalHistorySummaryEvent
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    return when (event) {
      is MedicalHistoryLoaded -> next(model.medicalHistoryLoaded(event.medicalHistory))
      is SummaryMedicalHistoryAnswerToggled -> noChange()
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
    }
  }
}
