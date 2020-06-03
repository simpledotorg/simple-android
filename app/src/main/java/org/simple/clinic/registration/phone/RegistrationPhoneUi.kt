package org.simple.clinic.registration.phone

import org.simple.clinic.user.OngoingRegistrationEntry

interface RegistrationPhoneUi {
  fun preFillUserDetails(ongoingEntry: OngoingRegistrationEntry)
  fun openRegistrationNameEntryScreen()
  fun showInvalidNumberError()
  fun showUnexpectedErrorMessage()
  fun showNetworkErrorMessage()
  fun hideAnyError()
  fun showProgressIndicator()
  fun hideProgressIndicator()
  fun openLoginPinEntryScreen()
  fun showLoggedOutOfDeviceDialog()
  fun showAccessDeniedScreen(number: String)
}
