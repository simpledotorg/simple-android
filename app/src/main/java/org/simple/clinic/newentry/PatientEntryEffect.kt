package org.simple.clinic.newentry

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientEntryValidationError

sealed class PatientEntryEffect

object FetchPatientEntry : PatientEntryEffect()

data class SavePatient(val entry: OngoingNewPatientEntry) : PatientEntryEffect()

object LoadInputFields : PatientEntryEffect()

object FetchColonyOrVillagesEffect : PatientEntryEffect()

sealed class PatientEntryViewEffect : PatientEntryEffect()

data class PrefillFields(val patientEntry: OngoingNewPatientEntry) : PatientEntryViewEffect()

object ScrollFormOnGenderSelection : PatientEntryViewEffect()

data class ShowDatePatternInDateOfBirthLabel(val show: Boolean) : PatientEntryViewEffect()

object OpenMedicalHistoryEntryScreen : PatientEntryViewEffect()

data class SetupUi(val inputFields: InputFields) : PatientEntryViewEffect()

data class HideValidationError(val field: Field) : PatientEntryViewEffect()

data class ShowValidationErrors(val errors: List<PatientEntryValidationError>) : PatientEntryViewEffect()
