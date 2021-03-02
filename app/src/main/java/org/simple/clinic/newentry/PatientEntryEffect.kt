package org.simple.clinic.newentry

import org.simple.clinic.newentry.country.InputFields
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientEntryValidationError

sealed class PatientEntryEffect

object FetchPatientEntry : PatientEntryEffect()

data class PrefillFields(val patientEntry: OngoingNewPatientEntry) : PatientEntryEffect()

object ScrollFormOnGenderSelection : PatientEntryEffect()

data class ShowDatePatternInDateOfBirthLabel(val show: Boolean) : PatientEntryEffect()

data class ShowValidationErrors(val errors: List<PatientEntryValidationError>) : PatientEntryEffect()

data class HideValidationError(val field: Field) : PatientEntryEffect()

data class SavePatient(val entry: OngoingNewPatientEntry) : PatientEntryEffect()

object OpenMedicalHistoryEntryScreen : PatientEntryEffect()

object LoadInputFields : PatientEntryEffect()

data class SetupUi(val inputFields: InputFields) : PatientEntryEffect()

object FetchColonyOrVillagesEffect : PatientEntryEffect()
