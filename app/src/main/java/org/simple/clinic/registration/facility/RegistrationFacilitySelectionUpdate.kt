package org.simple.clinic.registration.facility

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update

class RegistrationFacilitySelectionUpdate: Update<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEvent, RegistrationFacilitySelectionEffect> {

  override fun update(
      model: RegistrationFacilitySelectionModel,
      event: RegistrationFacilitySelectionEvent
  ): Next<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {
    return noChange()
  }
}
