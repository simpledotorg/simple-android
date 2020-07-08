package org.simple.clinic.scheduleappointment.facilityselection

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class FacilitySelectionInit : Init<FacilitySelectionModel, FacilitySelectionEffect> {

  override fun init(model: FacilitySelectionModel): First<FacilitySelectionModel, FacilitySelectionEffect> {
    return first(model)
  }
}
