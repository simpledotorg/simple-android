package org.simple.clinic.registration.register

interface RegistrationLoadingUi: RegistrationLoadingUiActions {
  fun openHomeScreen()
  fun showNetworkError()
  fun showUnexpectedError()
}
