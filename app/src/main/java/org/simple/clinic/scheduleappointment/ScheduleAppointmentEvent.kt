package org.simple.clinic.scheduleappointment

import org.simple.clinic.facility.Facility
import org.simple.clinic.overdue.PotentialAppointmentDate
import org.simple.clinic.teleconsultlog.teleconsultrecord.TeleconsultRecord
import org.simple.clinic.widgets.UiEvent
import java.time.LocalDate

sealed class ScheduleAppointmentEvent : UiEvent

data class DefaultAppointmentDateLoaded(val potentialAppointmentDate: PotentialAppointmentDate) : ScheduleAppointmentEvent()

data object AppointmentDateIncremented : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Increment appointment due date"
}

data object AppointmentDateDecremented : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Decrement appointment due date"
}

data class AppointmentCalendarDateSelected(val selectedDate: LocalDate) : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Appointment calendar date selected"
}

data object ManuallySelectAppointmentDateClicked : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Manually Select Appointment Date"
}

data class AppointmentFacilitiesLoaded(
    val assignedFacility: Facility?,
    val currentFacility: Facility
) : ScheduleAppointmentEvent()

data class PatientFacilityChanged(val facility: Facility) : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment: Patient facility changed"
}

data object AppointmentScheduled : ScheduleAppointmentEvent()

data object DoneClicked : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Appointment done"
}

data class PatientDefaulterStatusLoaded(val isPatientADefaulter: Boolean) : ScheduleAppointmentEvent()

data object SchedulingSkipped : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Scheduling skipped"
}

data class TeleconsultRecordLoaded(val teleconsultRecord: TeleconsultRecord?) : ScheduleAppointmentEvent()

data object NextClicked : ScheduleAppointmentEvent() {
  override val analyticsName = "Schedule Appointment:Next clicked"
}

data object AppointmentScheduledForPatientFromNext : ScheduleAppointmentEvent()

