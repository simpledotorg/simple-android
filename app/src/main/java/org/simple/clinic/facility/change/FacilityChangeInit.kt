package org.simple.clinic.facility.change

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class FacilityChangeInit : Init<FacilityChangeModel, FacilityChangeEffect> {

  override fun init(model: FacilityChangeModel): First<FacilityChangeModel, FacilityChangeEffect> {
    val effects = mutableSetOf<FacilityChangeEffect>()

    if (!model.hasLoadedCurrentFacility) {
      effects.add(LoadCurrentFacility)
    }

    return first(model, effects)
  }
}
