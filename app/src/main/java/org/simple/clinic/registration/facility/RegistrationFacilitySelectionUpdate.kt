package org.simple.clinic.registration.facility

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class RegistrationFacilitySelectionUpdate(
    private val showIntroVideoScreen: Boolean
) : Update<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEvent, RegistrationFacilitySelectionEffect> {

  override fun update(
      model: RegistrationFacilitySelectionModel,
      event: RegistrationFacilitySelectionEvent
  ): Next<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {
    return when (event) {
      is RegistrationFacilityClicked -> dispatch(OpenConfirmFacilitySheet(event.facility))
      is RegistrationFacilityConfirmed -> registrationFacilityConfirmed(model, event)
    }
  }
  
  private fun registrationFacilityConfirmed(
      model: RegistrationFacilitySelectionModel,
      event: RegistrationFacilityConfirmed
  ): Next<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {
    val updatedEntry = model.ongoingEntry.withFacilityUuid(event.facilityUuid)
    val effect = if (showIntroVideoScreen) {
      MoveToIntroVideoScreen(updatedEntry)
    } else {
      MoveToRegistrationLoadingScreen(updatedEntry)
    }

    return dispatch(effect)
  }
}
