package org.simple.clinic.forgotpin.confirmpin

interface ForgotPinConfirmPinUiActions {
  fun hideError()
  fun showPinMismatchedError()
  fun showProgress()
  fun showNetworkError()
  fun showUnexpectedError()
  fun goToHomeScreen()
}
