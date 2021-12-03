package org.simple.clinic.editpatient

import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import java.util.UUID

sealed class EditPatientEffect

data class FetchBpPassportsEffect(val patientUuid: UUID) : EditPatientEffect()

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

data class HideValidationErrorsEffect(
    val validationErrors: Set<EditPatientValidationError>
) : EditPatientViewEffect()

data class ShowValidationErrorsEffect(
    val validationErrors: Set<EditPatientValidationError>
) : EditPatientViewEffect()

object ShowDatePatternInDateOfBirthLabelEffect : EditPatientViewEffect()

object HideDatePatternInDateOfBirthLabelEffect : EditPatientViewEffect()

object GoBackEffect : EditPatientViewEffect()

object ShowDiscardChangesAlertEffect : EditPatientViewEffect()
