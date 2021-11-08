package org.simple.clinic.newentry

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientEntryValidationError

sealed class PatientEntryEffect

object FetchPatientEntry : PatientEntryEffect()

data class ShowValidationErrors(val errors: List<PatientEntryValidationError>) : PatientEntryEffect()

data class HideValidationError(val field: Field) : PatientEntryEffect()

data class SavePatient(val entry: OngoingNewPatientEntry) : PatientEntryEffect()

object OpenMedicalHistoryEntryScreen : PatientEntryEffect()

object LoadInputFields : PatientEntryEffect()

data class SetupUi(val inputFields: InputFields) : PatientEntryEffect()

object FetchColonyOrVillagesEffect : PatientEntryEffect()

sealed class PatientEntryViewEffect : PatientEntryEffect()

data class PrefillFields(val patientEntry: OngoingNewPatientEntry) : PatientEntryViewEffect()

object ScrollFormOnGenderSelection : PatientEntryViewEffect()

data class ShowDatePatternInDateOfBirthLabel(val show: Boolean) : PatientEntryViewEffect()
