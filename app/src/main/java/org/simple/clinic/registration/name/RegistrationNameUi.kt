package org.simple.clinic.registration.name

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationNameUi {
  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry)
  fun showEmptyNameValidationError()
  fun hideValidationError()
  fun openRegistrationPinEntryScreen()
}
