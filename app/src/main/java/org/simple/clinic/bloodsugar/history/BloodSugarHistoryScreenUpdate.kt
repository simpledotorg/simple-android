package org.simple.clinic.bloodsugar.history

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class BloodSugarHistoryScreenUpdate : Update<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEvent, BloodSugarHistoryScreenEffect> {
  override fun update(
      model: BloodSugarHistoryScreenModel,
      event: BloodSugarHistoryScreenEvent
  ): Next<BloodSugarHistoryScreenModel, BloodSugarHistoryScreenEffect> {
    return when (event) {
      is PatientLoaded -> next(model.patientLoaded(event.patient))
      is AddNewBloodSugarClicked -> dispatch(OpenBloodSugarEntrySheet(model.patientUuid))
      is BloodSugarClicked -> dispatch(OpenBloodSugarUpdateSheet(event.bloodSugarMeasurement))
    }
  }
}
