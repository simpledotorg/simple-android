package org.simple.clinic.scheduleappointment.facilityselection

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class FacilitySelectionUpdate : Update<FacilitySelectionModel, FacilitySelectionEvent, FacilitySelectionEffect> {

  override fun update(
      model: FacilitySelectionModel,
      event: FacilitySelectionEvent
  ): Next<FacilitySelectionModel, FacilitySelectionEffect> {
    return when (event) {
      is FacilitySelected -> dispatch(ForwardSelectedFacility(event.facility))
    }
  }
}
