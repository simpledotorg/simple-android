package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap

sealed class TeleconsultDoctorInfoEvent

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultDoctorInfoEvent()

data class SignatureBitmapLoaded(val signatureBitmap: Bitmap?) : TeleconsultDoctorInfoEvent()
