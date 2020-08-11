package org.simple.clinic.summary.updatephone

import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.widgets.UiEvent

sealed class UpdatePhoneNumberEvent : UiEvent

data class PhoneNumberLoaded(val phoneNumber: String) : UpdatePhoneNumberEvent()

data class PhoneNumberValidated(val phoneNumber: String, val result: PhoneNumberValidator.Result) : UpdatePhoneNumberEvent()

object NewPhoneNumberSaved : UpdatePhoneNumberEvent()
