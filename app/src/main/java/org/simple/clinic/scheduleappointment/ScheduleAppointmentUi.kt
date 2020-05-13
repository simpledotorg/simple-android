package org.simple.clinic.scheduleappointment

import org.simple.clinic.overdue.TimeToAppointment
import org.threeten.bp.LocalDate

interface ScheduleAppointmentUi {
  fun closeSheet()
  fun updateScheduledAppointment(appointmentDate: LocalDate, timeToAppointment: TimeToAppointment)
  fun enableIncrementButton(state: Boolean)
  fun enableDecrementButton(state: Boolean)
  fun showManualDateSelector(date: LocalDate)
  fun showPatientFacility(facilityName: String)
}
