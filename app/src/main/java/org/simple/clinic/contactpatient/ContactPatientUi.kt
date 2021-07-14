package org.simple.clinic.contactpatient

import org.simple.clinic.overdue.TimeToAppointment
import org.simple.clinic.patient.Gender
import java.time.LocalDate

interface ContactPatientUi {
  fun switchToCallPatientView_Old()
  fun switchToCallPatientView()

  fun switchToSetAppointmentReminderView()

  fun renderPatientDetails_Old(name: String, gender: Gender, age: Int, phoneNumber: String)
  fun showCallResultSection_Old()
  fun hideCallResultSection_Old()
  fun showSecureCallUi_Old()
  fun hideSecureCallUi_Old()

  fun showPatientWithNoPhoneNumberUi()
  fun hidePatientWithPhoneNumberUi()

  fun renderSelectedAppointmentDate(
      selectedAppointmentReminderPeriod: TimeToAppointment,
      selectedDate: LocalDate
  )

  fun disablePreviousReminderDateStepper()
  fun enablePreviousReminderDateStepper()
  fun disableNextReminderDateStepper()
  fun enableNextReminderDateStepper()
  fun showProgress()
  fun hideProgress()
}
