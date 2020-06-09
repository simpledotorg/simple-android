package org.simple.clinic.registration.phone

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationPhoneUiActions {
  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry)
  fun showAccessDeniedScreen(number: String)
  fun openLoginPinEntryScreen()
  fun showLoggedOutOfDeviceDialog()
  fun openRegistrationNameEntryScreen(currentRegistrationEntry: OngoingRegistrationEntry)
}
