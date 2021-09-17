package org.simple.clinic.summary.addphone

interface AddPhoneNumberUi {
  fun showPhoneNumberBlank()
  fun showPhoneNumberTooShortError(requiredNumberLength: Int)
  fun clearPhoneNumberError()
}
