package org.simple.clinic.summary.updatephone

import org.simple.clinic.widgets.UiEvent

sealed class UpdatePhoneNumberEvent : UiEvent

data class PhoneNumberLoaded(val phoneNumber: String) : UpdatePhoneNumberEvent()
