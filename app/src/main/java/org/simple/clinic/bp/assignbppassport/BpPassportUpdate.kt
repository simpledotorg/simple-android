package org.simple.clinic.bp.assignbppassport

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.patient.OngoingNewPatientEntry

class BpPassportUpdate : Update<BpPassportModel, BpPassportEvent, BpPassportEffect> {
  override fun update(model: BpPassportModel, event: BpPassportEvent): Next<BpPassportModel, BpPassportEffect> {
    return when (event) {
      RegisterNewPatientClicked -> {
        val ongoingNewPatientEntry = OngoingNewPatientEntry(identifier = model.identifier)
        dispatch(SaveNewOngoingPatientEntry(ongoingNewPatientEntry))
      }
      NewOngoingPatientEntrySaved -> dispatch(FetchCurrentFacility)
      is CurrentFacilityRetrieved -> dispatch(OpenPatientEntryScreen(event.facility))
    }
  }
}
