package org.simple.clinic.facility.change

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class FacilityChangeUpdate : Update<FacilityChangeModel, FacilityChangeEvent, FacilityChangeEffect> {

  override fun update(model: FacilityChangeModel, event: FacilityChangeEvent): Next<FacilityChangeModel, FacilityChangeEffect> {
    return noChange()
  }
}
