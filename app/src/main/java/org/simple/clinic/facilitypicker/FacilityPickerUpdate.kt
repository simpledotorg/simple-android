package org.simple.clinic.facilitypicker

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class FacilityPickerUpdate: Update<FacilityPickerModel, FacilityPickerEvent, FacilityPickerEffect> {

  override fun update(model: FacilityPickerModel, event: FacilityPickerEvent): Next<FacilityPickerModel, FacilityPickerEffect> {
    return noChange()
  }
}
