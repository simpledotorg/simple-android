package org.simple.clinic.patient

sealed class PatientSearchCriteria {

  data class ByName(val patientName: String): PatientSearchCriteria()

  data class ByPhoneNumber(val phoneNumber: String): PatientSearchCriteria()
}
