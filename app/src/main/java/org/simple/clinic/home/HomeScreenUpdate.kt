package org.simple.clinic.home

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class HomeScreenUpdate : Update<HomeScreenModel, HomeScreenEvent, HomeScreenEffect> {

  override fun update(model: HomeScreenModel, event: HomeScreenEvent): Next<HomeScreenModel, HomeScreenEffect> {
    return when (event) {
      HomeFacilitySelectionClicked -> dispatch(OpenFacilitySelection)
      is CurrentFacilityLoaded -> next(model.facilityLoaded(event.facility))
      is OverdueAppointmentCountLoaded -> next(model.overdueAppointmentCountLoaded(event.overdueAppointmentCount))
      is PatientSearchByIdentifierCompleted -> scannedPatientBusinessId(event)
    }
  }

  private fun scannedPatientBusinessId(event: PatientSearchByIdentifierCompleted): Next<HomeScreenModel, HomeScreenEffect> {
    return dispatch(OpenPatientSummary(event.patient.get().uuid))
  }
}
