package org.simple.clinic.forgotpin.confirmpin

interface ForgotPinConfirmPinUi : UiActions {
  fun showUserName(name: String)
  fun showFacility(name: String)
  fun showUnexpectedError()
  fun goToHomeScreen()
}
