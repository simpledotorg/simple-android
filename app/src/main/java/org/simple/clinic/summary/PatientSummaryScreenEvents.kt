package org.simple.clinic.summary

import org.simple.clinic.widgets.UiEvent

class ScheduleAppointmentSheetClosed : UiEvent {
  override val analyticsName = "Patient Summary:Schedule Appointment Sheet Closed"
}

object PatientSummaryBloodPressureSaved : UiEvent

object PatientSummaryLinkIdCompleted : UiEvent
