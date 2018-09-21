package org.simple.clinic.home.overdue.appointmentreminder

import org.simple.clinic.widgets.UiEvent
import java.util.UUID

data class AppointmentReminderSheetCreated(val initialIndex: Int, val appointmentUuid: UUID) : UiEvent

data class ReminderDateIncremented(val currentIndex: Int, val size: Int) : UiEvent {
  override val analyticsName = "Appointment Reminder:Increment reminder date"
}

data class ReminderDateDecremented(val currentIndex: Int, val size: Int) : UiEvent {
  override val analyticsName = "Appointment Reminder:Decrement reminder date"
}

data class ReminderCreated(val selectedReminderState: AppointmentReminder) : UiEvent {
  override val analyticsName = "Appointment Reminder:Reminder scheduled"
}
