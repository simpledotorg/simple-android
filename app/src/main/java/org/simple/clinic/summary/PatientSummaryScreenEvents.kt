package org.simple.clinic.summary

import org.simple.clinic.bp.BloodPressureMeasurement
import org.simple.clinic.medicalhistory.MedicalHistory
import org.simple.clinic.medicalhistory.MedicalHistoryQuestion
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import java.util.UUID

data class PatientSummaryScreenCreated(val patientUuid: UUID, val caller: PatientSummaryCaller, val screenCreatedTimestamp: Instant) : UiEvent

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

object AppointmentScheduled : UiEvent

data class SummaryMedicalHistoryAnswerToggled(val question: MedicalHistoryQuestion, val answer: MedicalHistory.Answer) : UiEvent {
  override val analyticsName = "Patient Summary:Answer for $question set to $answer"
}

data class PatientSummaryRestoredWithBPSaved(val wasBloodPressureSaved: Boolean) : UiEvent

data class PatientSummaryBpClicked(val bloodPressureMeasurement: BloodPressureMeasurement): UiEvent

data class PatientSummaryItemChanged(val patientSummaryItems: PatientSummaryItems): UiEvent

data class PatientSummaryBloodPressureSaved(val wasBloodPressureSaved: Boolean): UiEvent

data class PatientSummaryAllBloodPressuresDeleted(val allBloodPressuresDeleted: Boolean): UiEvent
