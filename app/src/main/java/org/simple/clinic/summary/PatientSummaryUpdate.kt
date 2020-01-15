package org.simple.clinic.summary

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.next

class PatientSummaryUpdate : Update<PatientSummaryModel, PatientSummaryEvent, PatientSummaryEffect> {

  override fun update(model: PatientSummaryModel, event: PatientSummaryEvent): Next<PatientSummaryModel, PatientSummaryEffect> {
    return when (event) {
      is PatientSummaryProfileLoaded -> next(model.patientSummaryProfileLoaded(event.patientSummaryProfile))
      is PatientSummaryBackClicked -> noChange()
      is PatientSummaryDoneClicked -> noChange()
    }
  }
}
