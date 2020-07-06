package org.simple.clinic.facilitypicker

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class FacilityPickerInit: Init<FacilityPickerModel, FacilityPickerEffect> {

  override fun init(model: FacilityPickerModel): First<FacilityPickerModel, FacilityPickerEffect> {
    return first(model)
  }
}
