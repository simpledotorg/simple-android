package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap

sealed class TeleconsultDoctorInfoEffect

object LoadMedicalRegistrationId : TeleconsultDoctorInfoEffect()

data class SetMedicalRegistrationId(val medicalRegistrationId: String) : TeleconsultDoctorInfoEffect()

object LoadSignatureBitmap : TeleconsultDoctorInfoEffect()

data class SetSignatureBitmap(val bitmap: Bitmap) : TeleconsultDoctorInfoEffect()

object LoadCurrentUser : TeleconsultDoctorInfoEffect()
