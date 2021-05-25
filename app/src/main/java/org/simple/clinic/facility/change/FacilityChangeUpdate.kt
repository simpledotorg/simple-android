package org.simple.clinic.facility.change

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class FacilityChangeUpdate : Update<FacilityChangeModel, FacilityChangeEvent, FacilityChangeEffect> {

  override fun update(
      model: FacilityChangeModel,
      event: FacilityChangeEvent
  ): Next<FacilityChangeModel, FacilityChangeEffect> {
    return when (event) {
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.facility))
      is FacilityChangeClicked -> facilitySelected(model, event)
    }
  }

  private fun facilitySelected(
      model: FacilityChangeModel,
      event: FacilityChangeClicked
  ): Next<FacilityChangeModel, FacilityChangeEffect> {
    return if (model.hasLoadedCurrentFacility)
      changeCurrentFacility(model, event)
    else
      noChange()
  }

  private fun changeCurrentFacility(
      model: FacilityChangeModel,
      event: FacilityChangeClicked
  ): Next<FacilityChangeModel, FacilityChangeEffect> {
    val currentFacility = model.currentFacility!!
    val selectedFacility = event.facility

    val hasSelectedADifferentFacility = selectedFacility.uuid != currentFacility.uuid

    val effect = if (hasSelectedADifferentFacility) OpenConfirmFacilityChangeSheet(selectedFacility) else GoBack

    return dispatch(effect)
  }
}
