package org.simple.clinic.login.pin

interface LoginPinScreenUi : UiActions {
  fun showPhoneNumber(phoneNumber: String)
  fun goBackToRegistrationScreen()
}
