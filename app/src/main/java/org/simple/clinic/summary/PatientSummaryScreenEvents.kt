package org.simple.clinic.summary

import org.simple.clinic.widgets.UiEvent

class PatientSummaryBackClicked : UiEvent {
  override val analyticsName = "Patient Summary:Back Clicked"
}

class PatientSummaryDoneClicked : UiEvent {
  override val analyticsName = "Patient Summary:Done Clicked"
}

class ScheduleAppointmentSheetClosed : UiEvent {
  override val analyticsName = "Patient Summary:Schedule Appointment Sheet Closed"
}

object PatientSummaryBloodPressureSaved : UiEvent

object PatientSummaryLinkIdCancelled : UiEvent

object PatientSummaryLinkIdCompleted : UiEvent
