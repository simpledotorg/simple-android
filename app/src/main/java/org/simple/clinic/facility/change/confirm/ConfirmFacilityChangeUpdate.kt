package org.simple.clinic.facility.change.confirm

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class ConfirmFacilityChangeUpdate : Update<ConfirmFacilityChangeModel, ConfirmFacilityChangeEvent, ConfirmFacilityChangeEffect> {
  override fun update(
      model: ConfirmFacilityChangeModel,
      event: ConfirmFacilityChangeEvent
  ): Next<ConfirmFacilityChangeModel, ConfirmFacilityChangeEffect> {
    return when (event) {
      is FacilityChangeConfirmed -> dispatch(ChangeFacilityEffect(event.selectedFacility))
      is FacilityChanged -> closeSheet(model, event)
      is CurrentFacilityLoaded -> next(model.currentFacilityLoaded(event.currentFacility))
      FacilitySyncGroupSwitchedAtTimeTouched -> dispatch(CloseSheet)
    }
  }

  private fun closeSheet(
      model: ConfirmFacilityChangeModel,
      event: FacilityChanged
  ): Next<ConfirmFacilityChangeModel, ConfirmFacilityChangeEffect> {
    val effect = if (model.hasFacilitySyncGroupSwitched(event.newFacility))
      TouchFacilitySyncGroupSwitchedAtTime
    else
      CloseSheet

    return dispatch(effect)
  }
}
