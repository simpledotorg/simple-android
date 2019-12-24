package org.simple.clinic.editpatient

import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId

sealed class EditPatientEffect

data class FetchBangladeshNationalIdEffect(val patient: Patient) : EditPatientEffect()

data class PrefillBangladeshNationalIdEffect(val bangladeshNationalId: String) : EditPatientEffect()

data class PrefillFormEffect(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?
) : EditPatientEffect()

data class HideValidationErrorsEffect(
    val validationErrors: Set<EditPatientValidationError>
) : EditPatientEffect()

object ShowDatePatternInDateOfBirthLabelEffect : EditPatientEffect()

object HideDatePatternInDateOfBirthLabelEffect : EditPatientEffect()

object GoBackEffect : EditPatientEffect()

object ShowDiscardChangesAlertEffect : EditPatientEffect()

data class ShowValidationErrorsEffect(
    val validationErrors: Set<EditPatientValidationError>
) : EditPatientEffect()

data class SavePatientEffect(
    val ongoingEntry: EditablePatientEntry,
    val savedPatient: Patient,
    val savedAddress: PatientAddress,
    val savedPhoneNumber: PatientPhoneNumber?,
    val bangladeshNationalId: BusinessId?
) : EditPatientEffect()
