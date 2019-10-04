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

object HidePhoneLengthErrors : PatientEntryEffect()

object HideDateOfBirthErrors : PatientEntryEffect()

object HideEmptyDateOfBirthAndAgeError : PatientEntryEffect()

object HideMissingGenderError : PatientEntryEffect()

object HideEmptyColonyOrVillageError : PatientEntryEffect()

object HideEmptyDistrictError : PatientEntryEffect()

object HideEmptyStateError : PatientEntryEffect()

data class ShowDatePatternInDateOfBirthLabel(val show: Boolean) : PatientEntryEffect()

data class SavePatient(val entry: OngoingNewPatientEntry) : PatientEntryEffect()

data class ShowValidationErrors(val errors: List<PatientEntryValidationError>) : PatientEntryEffect()
