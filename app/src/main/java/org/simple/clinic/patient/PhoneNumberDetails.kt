package org.simple.clinic.patient

// TODO: 01/06/20 Make this an embedded model in `PatientPhoneNumber`
data class PhoneNumberDetails(
    val number: String,
    val type: PatientPhoneNumberType
) {

  companion object {
    fun mobile(number: String) = PhoneNumberDetails(number, PatientPhoneNumberType.Mobile)
  }
}
