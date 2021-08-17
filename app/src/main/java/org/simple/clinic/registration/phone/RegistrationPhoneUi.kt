package org.simple.clinic.registration.phone

interface RegistrationPhoneUi {
  fun showInvalidNumberError()
  fun showUnexpectedErrorMessage()
  fun showNetworkErrorMessage()
  fun hideAnyError()
  fun showProgressIndicator()
  fun hideProgressIndicator()
}
