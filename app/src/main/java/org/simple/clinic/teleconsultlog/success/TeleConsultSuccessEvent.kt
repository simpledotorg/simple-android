package org.simple.clinic.teleconsultlog.success

import org.simple.clinic.patient.Patient
import org.simple.clinic.widgets.UiEvent

sealed class TeleConsultSuccessEvent : UiEvent

data class PatientDetailsLoaded(val patient: Patient) : TeleConsultSuccessEvent()

object NoPrescriptionClicked : TeleConsultSuccessEvent() {
  override val analyticsName: String = "Teleconsult Log:No Prescription Clicked"
}

object YesPrescriptionClicked : TeleConsultSuccessEvent() {
  override val analyticsName: String = "Teleconsult Log:Yes Prescription Clicked"
}
