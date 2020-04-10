package org.simple.clinic.patientcontact

import org.simple.clinic.phone.Dialer

interface PatientContactUiActions {
  fun directlyCallPatient(patientPhoneNumber: String, dialer: Dialer)
  fun maskedCallPatient(patientPhoneNumber: String, proxyNumber: String, dialer: Dialer)
}
