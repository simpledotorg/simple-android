package org.simple.clinic.registration.facility

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next

class RegistrationFacilitySelectionUpdate : Update<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEvent, RegistrationFacilitySelectionEffect> {

  override fun update(
      model: RegistrationFacilitySelectionModel,
      event: RegistrationFacilitySelectionEvent
  ): Next<RegistrationFacilitySelectionModel, RegistrationFacilitySelectionEffect> {
    return when (event) {
      is RegistrationFacilityClicked -> dispatch(OpenConfirmFacilitySheet(event.facility))
      is RegistrationFacilityConfirmed -> {
        val updatedEntry = model.ongoingEntry.withFacilityUuid(event.facilityUuid)

        next(model.withUpdatedEntry(updatedEntry),SaveRegistrationEntryAsUser(updatedEntry))
      }
      is CurrentRegistrationEntrySaved -> dispatch(MoveToIntroVideoScreen(model.ongoingEntry))
    }
  }
}
