package org.simple.clinic.registration.pin

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationPinUiActions {
  fun openRegistrationConfirmPinScreen(registrationEntry: OngoingRegistrationEntry)
}
