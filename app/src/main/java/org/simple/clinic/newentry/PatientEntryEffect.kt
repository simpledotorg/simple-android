package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientEntryValidationError

sealed class PatientEntryEffect

data object FetchPatientEntry : PatientEntryEffect()

data class SavePatient(val entry: OngoingNewPatientEntry) : PatientEntryEffect()

data object LoadInputFields : PatientEntryEffect()

data object FetchColonyOrVillagesEffect : PatientEntryEffect()

sealed class PatientEntryViewEffect : PatientEntryEffect()

data class PrefillFields(val patientEntry: OngoingNewPatientEntry) : PatientEntryViewEffect()

data object ScrollFormOnGenderSelection : PatientEntryViewEffect()

data class ShowDatePatternInDateOfBirthLabel(val show: Boolean) : PatientEntryViewEffect()

data object OpenMedicalHistoryEntryScreen : PatientEntryViewEffect()

data class HideValidationError(val field: Field) : PatientEntryViewEffect()

data class ShowValidationErrors(val errors: List<PatientEntryValidationError>) : PatientEntryViewEffect()
