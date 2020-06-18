package org.simple.clinic.registration.confirmpin

interface RegistrationConfirmPinUi {
  fun showPinMismatchError()
  fun clearPin()
  fun openFacilitySelectionScreen()
  fun goBackToPinScreen()
}
