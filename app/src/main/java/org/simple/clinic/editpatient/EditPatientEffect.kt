package org.simple.clinic.editpatient

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import java.util.UUID

sealed class EditPatientEffect

data class FetchBpPassportsEffect(val patientUuid: UUID) : EditPatientEffect()

object HideDatePatternInDateOfBirthLabelEffect : EditPatientEffect()

object GoBackEffect : EditPatientEffect()

object ShowDiscardChangesAlertEffect : EditPatientEffect()

data class SavePatientEffect(
    val ongoingEntry: EditablePatientEntry,
    val savedPatient: Patient,
    val savedAddress: PatientAddress,
    val savedPhoneNumber: PatientPhoneNumber?,
    val saveAlternativeId: BusinessId?
) : EditPatientEffect()

object LoadInputFields : EditPatientEffect()

object FetchColonyOrVillagesEffect : EditPatientEffect()

sealed class EditPatientViewEffect : EditPatientEffect()

data class PrefillFormEffect(
    val patient: Patient,
    val address: PatientAddress,
    val phoneNumber: PatientPhoneNumber?,
    val alternativeId: BusinessId?
) : EditPatientViewEffect()

data class HideValidationErrorsEffect(
    val validationErrors: Set<EditPatientValidationError>
) : EditPatientViewEffect()

data class SetupUi(val inputFields: InputFields) : EditPatientViewEffect()

data class DisplayBpPassportsEffect(val bpPassports: List<BusinessId>) : EditPatientViewEffect()

data class ShowValidationErrorsEffect(
    val validationErrors: Set<EditPatientValidationError>
) : EditPatientViewEffect()

object ShowDatePatternInDateOfBirthLabelEffect : EditPatientViewEffect()
