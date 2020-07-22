package org.simple.clinic.registration.register

interface RegistrationLoadingUi: RegistrationLoadingUiActions {
  fun showNetworkError()
  fun showUnexpectedError()
}
