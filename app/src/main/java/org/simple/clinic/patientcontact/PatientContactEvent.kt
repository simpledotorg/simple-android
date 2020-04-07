package org.simple.clinic.patientcontact

import org.simple.clinic.patient.PatientProfile

sealed class PatientContactEvent

data class PatientProfileLoaded(
    val patientProfile: PatientProfile
): PatientContactEvent()
