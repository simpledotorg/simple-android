package org.simple.clinic.medicalhistory.newentry

import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.patient.PatientUuid
import org.simple.clinic.widgets.UiEvent

data class NewMedicalHistoryScreenCreated(val patientUuid: PatientUuid) : UiEvent {
  override val analyticsName = "New Medical History:Screen Created for $patientUuid"
}

class SaveMedicalHistoryClicked : UiEvent {
  override val analyticsName = "New Medical History:Save Clicked"
}
