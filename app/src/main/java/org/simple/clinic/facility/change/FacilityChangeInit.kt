package org.simple.clinic.facility.change

import com.spotify.mobius.First
import com.spotify.mobius.Init
import org.simple.clinic.mobius.first

class FacilityChangeInit : Init<FacilityChangeModel, FacilityChangeEffect> {

  override fun init(model: FacilityChangeModel): First<FacilityChangeModel, FacilityChangeEffect> {
    return first(model)
  }
}
