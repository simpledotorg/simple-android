package org.simple.clinic.forgotpin.createnewpin

interface ForgotPinCreateNewPinUi : UiActions {
  fun showUserName(name: String)
  fun showFacility(name: String)
  fun showInvalidPinError()
  fun showConfirmPinScreen(pin: String)
  fun hideInvalidPinError()
}