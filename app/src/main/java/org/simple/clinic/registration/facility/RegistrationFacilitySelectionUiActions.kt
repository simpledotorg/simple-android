package org.simple.clinic.registration.facility

import java.util.UUID

interface RegistrationFacilitySelectionUiActions {
  fun showConfirmFacilitySheet(facilityUuid: UUID, facilityName: String)
}
