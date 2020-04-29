package org.simple.clinic.summary.medicalhistory

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class MedicalHistorySummaryUpdate : Update<MedicalHistorySummaryModel, MedicalHistorySummaryEvent, MedicalHistorySummaryEffect> {

  override fun update(
      model: MedicalHistorySummaryModel,
      event: MedicalHistorySummaryEvent
  ): Next<MedicalHistorySummaryModel, MedicalHistorySummaryEffect> {
    return noChange()
  }
}
