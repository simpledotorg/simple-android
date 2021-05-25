package org.simple.clinic.editpatient

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import java.util.UUID

sealed class EditPatientEffect

data class PrefillFormEffect(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?,
    val alternativeId: BusinessId?
) : EditPatientEffect()

data class HideValidationErrorsEffect(
    val validationErrors: Set<EditPatientValidationError>
) : EditPatientEffect()

data class FetchBpPassportsEffect(val patientUuid: UUID) : EditPatientEffect()

data class DisplayBpPassportsEffect(val bpPassports: List<BusinessId>) : EditPatientEffect()

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
    val saveAlternativeId: BusinessId?
) : EditPatientEffect()

object LoadInputFields : EditPatientEffect()

data class SetupUi(val inputFields: InputFields) : EditPatientEffect()

object FetchColonyOrVillagesEffect : EditPatientEffect()
