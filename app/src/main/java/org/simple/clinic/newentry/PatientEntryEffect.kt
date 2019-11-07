package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientEntryValidationError

sealed class PatientEntryEffect

object FetchPatientEntry : PatientEntryEffect()

data class PrefillFields(
    val patientEntry: OngoingNewPatientEntry
) : PatientEntryEffect()

object ScrollFormToBottom : PatientEntryEffect()

data class ShowEmptyFullNameError(val show: Boolean) : PatientEntryEffect()

object HideEmptyDateOfBirthAndAgeError : PatientEntryEffect()

data class ShowDatePatternInDateOfBirthLabel(val show: Boolean) : PatientEntryEffect()

data class SavePatient(val entry: OngoingNewPatientEntry) : PatientEntryEffect()

data class ShowValidationErrors(val errors: List<PatientEntryValidationError>) : PatientEntryEffect()

object OpenMedicalHistoryEntryScreen : PatientEntryEffect()

data class HideError(val field: Field) : PatientEntryEffect()

enum class Field {
  PhoneNumber, DateOfBirth, Gender, ColonyOrVillage, District, State
}
