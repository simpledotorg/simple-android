package org.simple.clinic.forgotpin.createnewpin

interface UiActions {
  fun showInvalidPinError()
  fun showConfirmPinScreen(pin: String)
  fun hideInvalidPinError()
}
