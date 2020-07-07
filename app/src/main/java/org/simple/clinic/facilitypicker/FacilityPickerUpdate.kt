package org.simple.clinic.facilitypicker

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class FacilityPickerUpdate(
    private val pickFrom: PickFrom
) : Update<FacilityPickerModel, FacilityPickerEvent, FacilityPickerEffect> {

  override fun update(model: FacilityPickerModel, event: FacilityPickerEvent): Next<FacilityPickerModel, FacilityPickerEffect> {
    return when (event) {
      is LocationFetched -> next(model.locationFetched(event.update))
      is FacilitiesFetched -> next(model.queryChanged(event.query).facilitiesLoaded(event.facilities))
      is SearchQueryChanged -> dispatch(LoadFacilitiesWithQuery(event.query))
      is TotalFacilityCountLoaded -> next(model.facilityCountLoaded(event.count))
      is FacilityClicked -> dispatch(ForwardSelectedFacility(event.facility))
    }
  }
}
