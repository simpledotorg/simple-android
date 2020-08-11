package org.simple.clinic.summary.updatephone

import org.simple.clinic.patient.PatientUuid

sealed class UpdatePhoneNumberEffect

data class PrefillPhoneNumber(val phoneNumber: String) : UpdatePhoneNumberEffect()

data class LoadPhoneNumber(val patientUuid: PatientUuid) : UpdatePhoneNumberEffect()

data class ValidatePhoneNumber(val phoneNumber: String) : UpdatePhoneNumberEffect()
