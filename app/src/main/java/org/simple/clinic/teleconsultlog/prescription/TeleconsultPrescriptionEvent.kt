package org.simple.clinic.teleconsultlog.prescription

import android.graphics.Bitmap
import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultPrescriptionEvent : UiEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleconsultPrescriptionEvent()

object BackClicked : TeleconsultPrescriptionEvent() {
  override val analyticsName: String = "Teleconsult Prescription:Back Clicked"
}

data class DataForNextClickLoaded(
    val medicalInstructions: String,
    val medicalRegistrationId: String,
    val signatureBitmap: Bitmap?
) : TeleconsultPrescriptionEvent()

data class NextButtonClicked(val medicalInstructions: String, val medicalRegistrationId: String) : TeleconsultPrescriptionEvent() {
  override val analyticsName: String = "Teleconsult Prescription:Next Clicked"
}
