package org.simple.clinic.scheduleappointment

import org.simple.clinic.overdue.TimeToAppointment
import java.time.LocalDate

interface ScheduleAppointmentUi {
  fun updateScheduledAppointment(appointmentDate: LocalDate, timeToAppointment: TimeToAppointment)
  fun enableIncrementButton(state: Boolean)
  fun enableDecrementButton(state: Boolean)
  fun showPatientFacility(facilityName: String)
  fun showProgress()
  fun hideProgress()
  fun showDoneButton()
  fun showNextButton()
  fun hideDoneButton()
  fun hideNextButton()
  fun showNextButtonProgress()
  fun hideNextButtonProgress()
}
