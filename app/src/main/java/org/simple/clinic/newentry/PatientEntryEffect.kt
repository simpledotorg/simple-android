package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry

sealed class PatientEntryEffect

object FetchPatientEntry : PatientEntryEffect()

data class PrefillFields(val patientEntry: OngoingNewPatientEntry) : PatientEntryEffect()

object ScrollFormOnGenderSelection : PatientEntryEffect()

data class ShowDatePatternInDateOfBirthLabel(val show: Boolean) : PatientEntryEffect()

data class HideValidationError(val field: Field) : PatientEntryEffect()

data class SavePatient(val entry: OngoingNewPatientEntry) : PatientEntryEffect()

object OpenMedicalHistoryEntryScreen : PatientEntryEffect()
