package org.simple.clinic.registration.confirmpin

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationConfirmPinUiActions {
  fun clearPin()
  fun openFacilitySelectionScreen()
  fun goBackToPinScreen(entry: OngoingRegistrationEntry)
}
