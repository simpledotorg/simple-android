package org.simple.clinic.summary.updatephone

import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.widgets.UiEvent

sealed class UpdatePhoneNumberEvent : UiEvent

data class PhoneNumberLoaded(val phoneNumber: String) : UpdatePhoneNumberEvent()

data class PhoneNumberValidated(
    val phoneNumber: String,
    val result: PhoneNumberValidator.Result
) : UpdatePhoneNumberEvent()

object NewPhoneNumberSaved : UpdatePhoneNumberEvent()

data class UpdatePhoneNumberSaveClicked(val number: String) : UpdatePhoneNumberEvent() {
  override val analyticsName = "Patient Summary:Update Phone Number:Save Clicked"
}

object ExistingPhoneNumberSaved : UpdatePhoneNumberEvent()

object UpdatePhoneNumberCancelClicked : UpdatePhoneNumberEvent() {
  override val analyticsName = "Patient Summary:Update Phone Number:Cancel Clicked"
}
