package org.simple.clinic.contactpatient

import org.simple.clinic.overdue.TimeToAppointment
import java.time.LocalDate

interface ContactPatientUi {
  fun showProgress()
  fun hideProgress()

  /**
   * Call patient view
   */
  fun switchToCallPatientView()
  fun switchToSetAppointmentReminderView()

  fun renderPatientDetails(patientDetails: PatientDetails)

  fun showSecureCallUi()
  fun hideSecureCallUi()

  fun showPatientWithNoPhoneNumberUi()
  fun hidePatientWithNoPhoneNumberUi()

  fun showPatientWithPhoneNumberUi()
  fun hidePatientWithPhoneNumberUi()

  fun setResultOfCallLabelText()
  fun setResultLabelText()

  fun setRegisterAtLabelText()
  fun setTransferredFromLabelText()

  fun showPatientWithPhoneNumberCallResults()
  fun hidePatientWithPhoneNumberCallResults()

  fun showPatientWithNoPhoneNumberResults()

  /**
   * Select reminder view
   */
  fun renderSelectedAppointmentDate(
      selectedAppointmentReminderPeriod: TimeToAppointment,
      selectedDate: LocalDate
  )

  fun disablePreviousReminderDateStepper()
  fun enablePreviousReminderDateStepper()
  fun disableNextReminderDateStepper()
  fun enableNextReminderDateStepper()
}
