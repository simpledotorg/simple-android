package org.simple.clinic.summary.addphone

import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.widgets.UiEvent

sealed class AddPhoneNumberEvent : UiEvent

object PhoneNumberAdded : AddPhoneNumberEvent()

data class PhoneNumberValidated(val newNumber: String, val validationResult: PhoneNumberValidator.Result) : AddPhoneNumberEvent()
