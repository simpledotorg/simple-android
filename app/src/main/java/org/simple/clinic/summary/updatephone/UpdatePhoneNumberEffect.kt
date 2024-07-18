package org.simple.clinic.summary.updatephone

import org.simple.clinic.patient.PatientUuid

sealed class UpdatePhoneNumberEffect

data class PrefillPhoneNumber(val phoneNumber: String) : UpdatePhoneNumberEffect()

data class LoadPhoneNumber(val patientUuid: PatientUuid) : UpdatePhoneNumberEffect()

data class ValidatePhoneNumber(val phoneNumber: String) : UpdatePhoneNumberEffect()

data object ShowBlankPhoneNumberError : UpdatePhoneNumberEffect()

data class ShowPhoneNumberTooShortError(val minimumAllowedNumberLength: Int) : UpdatePhoneNumberEffect()

data object CloseDialog : UpdatePhoneNumberEffect()

data class SaveNewPhoneNumber(
    val patientUuid: PatientUuid,
    val newPhoneNumber: String
) : UpdatePhoneNumberEffect()

data class SaveExistingPhoneNumber(val patientUuid: PatientUuid) : UpdatePhoneNumberEffect()
