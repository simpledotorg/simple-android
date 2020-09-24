package org.simple.clinic.teleconsultlog.prescription.doctorinfo

import android.graphics.Bitmap
import org.simple.clinic.user.User
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultDoctorInfoEvent : UiEvent

data class MedicalRegistrationIdLoaded(val medicalRegistrationId: String) : TeleconsultDoctorInfoEvent()

data class SignatureBitmapLoaded(val signatureBitmap: Bitmap?) : TeleconsultDoctorInfoEvent()

data class MedicalRegistrationIdChanged(val medicalRegistrationId: String) : TeleconsultDoctorInfoEvent() {
  override val analyticsName: String = "Teleconsult Doctor Info:Medical Registration ID Changed"
}

data class MedicalInstructionsChanged(val instructions: String) : TeleconsultDoctorInfoEvent() {
  override val analyticsName: String = "Teleconsult Doctor Info:Medical Instructions Changed"
}

data class CurrentUserLoaded(val user: User) : TeleconsultDoctorInfoEvent()

object AddSignatureClicked : TeleconsultDoctorInfoEvent()

object ActivityResumed : TeleconsultDoctorInfoEvent()
