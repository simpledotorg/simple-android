package org.simple.clinic.contactpatient

import org.simple.clinic.phone.Dialer
import java.time.LocalDate

interface ContactPatientUiActions {
  fun directlyCallPatient(patientPhoneNumber: String, dialer: Dialer)
  fun maskedCallPatient(patientPhoneNumber: String, proxyNumber: String, dialer: Dialer)
  fun closeSheet()
  fun showManualDatePicker(preselectedDate: LocalDate, dateBounds: ClosedRange<LocalDate>)
}
