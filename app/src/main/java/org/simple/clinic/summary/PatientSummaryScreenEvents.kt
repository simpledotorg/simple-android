package org.simple.clinic.summary

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class PatientSummaryScreenCreated(val patientUuid: UUID, val caller: PatientSummaryCaller) : UiEvent

class PatientSummaryBackClicked : UiEvent {
  override val analyticsName = "Patient Summary:Back Clicked"
}

class PatientSummaryDoneClicked : UiEvent {
  override val analyticsName = "Patient Summary:Done Clicked"
}

class PatientSummaryNewBpClicked : UiEvent {
  override val analyticsName = "Patient Summary:New BP Clicked"
}

class PatientSummaryUpdateDrugsClicked : UiEvent {
  override val analyticsName = "Patient Summary:Update Drugs Clicked"
}

data class PatientSummaryBloodPressureClosed(val wasBloodPressureSaved: Boolean) : UiEvent {
  override val analyticsName = "Patient Summary:New Blood Pressure Saved"
}

class ScheduleAppointmentSheetClosed : UiEvent {
  override val analyticsName = "Patient Summary:Schedule Appointment Sheet Closed"
}

data class SummaryMedicalHistoryAnswerToggled(val question: MedicalHistoryQuestion, val selected: Boolean) : UiEvent {
  override val analyticsName = "Patient Summary:Answer for $question set to $selected"
}

data class PatientSummaryRestoredWithBPSaved(val wasBloodPressureSaved: Boolean) : UiEvent

data class PatientSummaryBpClicked(val bloodPressureMeasurement: BloodPressureMeasurement): UiEvent

