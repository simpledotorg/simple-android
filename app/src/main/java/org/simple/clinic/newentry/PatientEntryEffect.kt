package org.simple.clinic.newentry

import org.simple.clinic.patient.OngoingNewPatientEntry

sealed class PatientEntryEffect

object FetchPatientEntry : PatientEntryEffect()

data class PrefillFields(
    val patientEntry: OngoingNewPatientEntry
) : PatientEntryEffect()

object ScrollFormToBottom : PatientEntryEffect()
