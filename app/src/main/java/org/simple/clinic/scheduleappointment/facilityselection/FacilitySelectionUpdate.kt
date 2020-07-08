package org.simple.clinic.scheduleappointment.facilityselection

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class FacilitySelectionUpdate : Update<FacilitySelectionModel, FacilitySelectionEvent, FacilitySelectionEffect> {

  override fun update(model: FacilitySelectionModel, event: FacilitySelectionEvent): Next<FacilitySelectionModel, FacilitySelectionEffect> {
    return noChange()
  }
}
