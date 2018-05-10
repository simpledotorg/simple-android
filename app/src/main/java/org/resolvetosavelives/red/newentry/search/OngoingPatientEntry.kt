package org.resolvetosavelives.red.newentry.search

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val mobileNumber: String? = null
) {

  fun toPatient(): Patient {
    return Patient(personalDetails!!.fullName, mobileNumber!!)
  }

  data class PersonalDetails(val fullName: String)
}
