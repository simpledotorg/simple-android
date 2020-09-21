package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap
import org.simple.clinic.user.User

sealed class TeleconsultDoctorInfoEvent

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultDoctorInfoEvent()

data class SignatureBitmapLoaded(val signatureBitmap: Bitmap?) : TeleconsultDoctorInfoEvent()

data class MedicalRegistrationIdChanged(val medicalRegistrationId: String) : TeleconsultDoctorInfoEvent()

data class MedicalInstructionsChanged(val instructions: String) : TeleconsultDoctorInfoEvent()

data class CurrentUserLoaded(val user: User) : TeleconsultDoctorInfoEvent()
