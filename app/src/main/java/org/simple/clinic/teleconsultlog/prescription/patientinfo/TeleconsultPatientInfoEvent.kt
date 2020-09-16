package org.simple.clinic.teleconsultlog.prescription.patientinfo

import org.simple.clinic.patient.PatientProfile

sealed class TeleconsultPatientInfoEvent

data class PatientProfileLoaded(val patientProfile: PatientProfile) : TeleconsultPatientInfoEvent()
