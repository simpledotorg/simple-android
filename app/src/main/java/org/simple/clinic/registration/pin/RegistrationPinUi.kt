package org.simple.clinic.registration.pin

interface RegistrationPinUi: RegistrationPinUiActions {
  fun showIncompletePinError()
  fun hideIncompletePinError()
}
