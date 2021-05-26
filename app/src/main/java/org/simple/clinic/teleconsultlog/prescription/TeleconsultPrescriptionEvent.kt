package org.simple.clinic.teleconsultlog.prescription

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
    val hasSignatureBitmap: Boolean,
    val hasMedicines: Boolean
) : TeleconsultPrescriptionEvent()

data class NextButtonClicked(
    val medicalInstructions: String,
    val medicalRegistrationId: String
) : TeleconsultPrescriptionEvent() {
  override val analyticsName: String = "Teleconsult Prescription:Next Clicked"
}

data class TeleconsultIdAddedToPrescribedDrugs(
    val medicalInstructions: String
) : TeleconsultPrescriptionEvent()
