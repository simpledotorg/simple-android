package org.simple.clinic.teleconsultlog.prescription.doctorinfo

sealed class TeleconsultDoctorInfoEffect

object LoadMedicalRegistrationId : TeleconsultDoctorInfoEffect()

data class SetMedicalRegistrationId(val medicalRegistrationId: String) : TeleconsultDoctorInfoEffect()

object LoadSignatureBitmap : TeleconsultDoctorInfoEffect()
