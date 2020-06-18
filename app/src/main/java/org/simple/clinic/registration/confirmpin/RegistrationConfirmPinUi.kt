package org.simple.clinic.registration.confirmpin

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationConfirmPinUi : RegistrationConfirmPinUiActions {
  fun showPinMismatchError()
  fun goBackToPinScreen(entry: OngoingRegistrationEntry)
}
