package org.simple.clinic.teleconsultlog.prescription

import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class TeleconsultPrescriptionEvent : UiEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleconsultPrescriptionEvent()

object BackClicked : TeleconsultPrescriptionEvent() {
  override val analyticsName: String = "Teleconsult Prescription:Back Clicked"
}
