package org.simple.clinic.summary.addphone

import org.simple.clinic.widgets.UiEvent

sealed class AddPhoneNumberEvent : UiEvent

object PhoneNumberAdded : AddPhoneNumberEvent()
