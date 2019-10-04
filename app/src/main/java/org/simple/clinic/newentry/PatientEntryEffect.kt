package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry

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
