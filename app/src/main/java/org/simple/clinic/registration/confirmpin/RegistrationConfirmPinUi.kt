package org.simple.clinic.registration.confirmpin

interface RegistrationConfirmPinUi : RegistrationConfirmPinUiActions {
  fun showPinMismatchError()
  fun clearPin()
  fun openFacilitySelectionScreen()
  fun goBackToPinScreen()
}
