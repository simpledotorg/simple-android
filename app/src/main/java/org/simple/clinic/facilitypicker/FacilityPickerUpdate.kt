package org.simple.clinic.facilitypicker

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.facilitypicker.PickFrom.AllFacilities
import org.simple.clinic.facilitypicker.PickFrom.InCurrentGroup
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class FacilityPickerUpdate(
    private val pickFrom: PickFrom
) : Update<FacilityPickerModel, FacilityPickerEvent, FacilityPickerEffect> {

  override fun update(
      model: FacilityPickerModel,
      event: FacilityPickerEvent
  ): Next<FacilityPickerModel, FacilityPickerEffect> {
    return when (event) {
      is LocationFetched -> next(model.locationFetched(event.update))
      is FacilitiesFetched -> next(model.queryChanged(event.query).facilitiesLoaded(event.facilities))
      is SearchQueryChanged -> loadFacilities(event)
      is TotalFacilityCountLoaded -> next(model.facilityCountLoaded(event.count))
      is FacilityClicked -> dispatch(ForwardSelectedFacility(event.facility))
    }
  }

  private fun loadFacilities(event: SearchQueryChanged): Next<FacilityPickerModel, FacilityPickerEffect> {
    val effect = when (pickFrom) {
      AllFacilities -> LoadFacilitiesWithQuery(event.query)
      InCurrentGroup -> LoadFacilitiesInCurrentGroup(event.query)
    }

    return dispatch(effect)
  }
}
