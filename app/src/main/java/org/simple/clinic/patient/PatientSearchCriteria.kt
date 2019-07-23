package org.simple.clinic.patient

sealed class PatientSearchCriteria {

  data class Name(val patientName: String): PatientSearchCriteria()

  data class PhoneNumber(val phoneNumber: String): PatientSearchCriteria()
}
