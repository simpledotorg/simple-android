package org.resolvetosavelives.red.newentry.search

import java.util.UUID

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val mobileNumber: String? = null
) {

  fun toPatient(patientId: UUID): Patient {
    return Patient(patientId.toString(), personalDetails!!.fullName, mobileNumber!!)
  }

  data class PersonalDetails(val fullName: String)
}
