package org.simple.clinic.summary.addphone

import java.util.UUID

sealed class AddPhoneNumberEffect

data class AddPhoneNumber(val patientUuid: UUID, val newNumber: String) : AddPhoneNumberEffect()

object CloseDialog : AddPhoneNumberEffect()

data class ValidatePhoneNumber(val newNumber: String) : AddPhoneNumberEffect()
