package org.simple.clinic.summary.updatephone

sealed class UpdatePhoneNumberEffect

data class PrefillPhoneNumber(val phoneNumber: String) : UpdatePhoneNumberEffect()
