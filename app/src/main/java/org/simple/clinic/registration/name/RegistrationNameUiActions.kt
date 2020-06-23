package org.simple.clinic.registration.name

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationNameUiActions {
  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry)
  fun openRegistrationPinEntryScreen(registrationEntry: OngoingRegistrationEntry)
}
