package org.simple.clinic.teleconsultlog.prescription.doctorinfo

sealed class TeleconsultDoctorInfoEvent

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultDoctorInfoEvent()
