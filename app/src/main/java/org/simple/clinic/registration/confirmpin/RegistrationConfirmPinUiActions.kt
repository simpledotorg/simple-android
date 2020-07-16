package org.simple.clinic.registration.confirmpin

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationConfirmPinUiActions {
  fun clearPin()
  fun openFacilitySelectionScreen(entry: OngoingRegistrationEntry)
  fun goBackToPinScreen(entry: OngoingRegistrationEntry)
}
