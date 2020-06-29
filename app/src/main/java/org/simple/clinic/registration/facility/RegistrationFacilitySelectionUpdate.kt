package org.simple.clinic.registration.facility

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class RegistrationFacilitySelectionUpdate: Update<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEvent, RegistrationFacilitySelectionEffect> {

  override fun update(
      model: RegistrationFacilitySelectionModel,
      event: RegistrationFacilitySelectionEvent
  ): Next<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {
    return when(event) {
      is LocationFetched -> noChange()
      is FacilitiesFetched -> next(model.queryChanged(event.query).facilitiesLoaded(event.facilities))
      is RegistrationFacilitySearchQueryChanged -> dispatch(LoadFacilitiesWithQuery(event.query))
    }
  }
}
