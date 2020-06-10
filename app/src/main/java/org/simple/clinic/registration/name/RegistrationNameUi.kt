package org.simple.clinic.registration.name

interface RegistrationNameUi: RegistrationNameUiActions {
  fun showEmptyNameValidationError()
  fun hideValidationError()
  fun openRegistrationPinEntryScreen()
}
