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
      is BusinessIdScanned.ByShortCode -> dispatch(OpenShortCodeSearchScreen(event.shortCode))
      is BusinessIdScanned.ByPatientFound -> dispatch(OpenPatientSummary(event.patientId))
      is BusinessIdScanned.ByPatientNotFound -> dispatch(OpenPatientSearchScreen(event.identifier))
    }
  }
}
