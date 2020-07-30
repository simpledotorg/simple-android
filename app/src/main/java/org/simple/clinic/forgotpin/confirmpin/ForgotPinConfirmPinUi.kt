package org.simple.clinic.forgotpin.confirmpin

interface ForgotPinConfirmPinUi {
  fun showUserName(name: String)
  fun showFacility(name: String)
  fun showPinMismatchedError()
  fun showUnexpectedError()
  fun showNetworkError()
  fun hideError()
  fun showProgress()
  fun goToHomeScreen()
}
