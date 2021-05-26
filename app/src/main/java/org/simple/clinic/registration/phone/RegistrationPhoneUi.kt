package org.simple.clinic.registration.phone

interface RegistrationPhoneUi : RegistrationPhoneUiActions {
  fun showInvalidNumberError()
  fun showUnexpectedErrorMessage()
  fun showNetworkErrorMessage()
  fun hideAnyError()
  fun showProgressIndicator()
  fun hideProgressIndicator()
}
