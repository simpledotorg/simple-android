package org.simple.clinic.registration.facility

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class RegistrationFacilitySelectionInit: Init<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {

  override fun init(model: RegistrationFacilitySelectionModel): First<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {
    return first(model)
  }
}
