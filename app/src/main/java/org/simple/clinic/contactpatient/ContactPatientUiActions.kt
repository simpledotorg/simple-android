package org.simple.clinic.contactpatient

import org.simple.clinic.phone.Dialer
import org.threeten.bp.LocalDate

interface ContactPatientUiActions {
  fun directlyCallPatient(patientPhoneNumber: String, dialer: Dialer)
  fun maskedCallPatient(patientPhoneNumber: String, proxyNumber: String, dialer: Dialer)
  fun closeSheet()
  fun showManualDatePicker(preselectedDate: LocalDate, dateBounds: ClosedRange<LocalDate>)
}
