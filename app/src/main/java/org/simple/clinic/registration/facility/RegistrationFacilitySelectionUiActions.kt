package org.simple.clinic.registration.facility

import org.simple.clinic.user.OngoingRegistrationEntry
import java.util.UUID

interface RegistrationFacilitySelectionUiActions {
  fun showConfirmFacilitySheet(facilityUuid: UUID, facilityName: String)
  fun openIntroVideoScreen(registrationEntry: OngoingRegistrationEntry)
}
