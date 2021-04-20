package org.simple.clinic.contactpatient

import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.patient.Gender
import java.time.LocalDate

interface ContactPatientUi {
  fun switchToCallPatientView()
  fun switchToSetAppointmentReminderView()

  fun renderPatientDetails(name: String, gender: Gender, age: Int, phoneNumber: String)
  fun showCallResultSection()
  fun hideCallResultSection()
  fun showSecureCallUi()
  fun hideSecureCallUi()

  fun renderSelectedAppointmentDate(selectedAppointmentReminderPeriod: TimeToAppointment, selectedDate: LocalDate)
  fun disablePreviousReminderDateStepper()
  fun enablePreviousReminderDateStepper()
  fun disableNextReminderDateStepper()
  fun enableNextReminderDateStepper()
}
