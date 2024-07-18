package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap

sealed class TeleconsultDoctorInfoEffect

data object LoadMedicalRegistrationId : TeleconsultDoctorInfoEffect()

data class SetMedicalRegistrationId(val medicalRegistrationId: String) : TeleconsultDoctorInfoEffect()

data object LoadSignatureBitmap : TeleconsultDoctorInfoEffect()

data class SetSignatureBitmap(val bitmap: Bitmap) : TeleconsultDoctorInfoEffect()

data object LoadCurrentUser : TeleconsultDoctorInfoEffect()

data object ShowAddSignatureDialog : TeleconsultDoctorInfoEffect()

data object ShowAddSignatureButton : TeleconsultDoctorInfoEffect()
