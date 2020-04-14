package org.simple.clinic.contactpatient

import org.simple.clinic.phone.Dialer

interface ContactPatientUiActions {
  fun directlyCallPatient(patientPhoneNumber: String, dialer: Dialer)
  fun maskedCallPatient(patientPhoneNumber: String, proxyNumber: String, dialer: Dialer)
}
