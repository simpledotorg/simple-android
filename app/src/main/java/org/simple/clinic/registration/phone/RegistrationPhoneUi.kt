package org.simple.clinic.registration.phone

interface RegistrationPhoneUi: RegistrationPhoneUiActions {
  fun openRegistrationNameEntryScreen()
  fun showInvalidNumberError()
  fun showUnexpectedErrorMessage()
  fun showNetworkErrorMessage()
  fun hideAnyError()
  fun showProgressIndicator()
  fun hideProgressIndicator()
  fun showLoggedOutOfDeviceDialog()
}
