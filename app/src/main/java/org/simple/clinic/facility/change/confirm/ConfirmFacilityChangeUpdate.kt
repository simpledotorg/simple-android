package org.simple.clinic.facility.change.confirm

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class ConfirmFacilityChangeUpdate : Update<ConfirmFacilityChangeModel, ConfirmFacilityChangeEvent, ConfirmFacilityChangeEffect> {
  override fun update(
      model: ConfirmFacilityChangeModel,
      event: ConfirmFacilityChangeEvent
  ): Next<ConfirmFacilityChangeModel, ConfirmFacilityChangeEffect> {
    return when (event) {
      is FacilityChangeConfirmed -> dispatch(ChangeFacilityEffect(event.selectedFacility))
      FacilityChanged -> dispatch(CloseSheet)
      is CurrentFacilityLoaded -> noChange()
    }
  }
}
