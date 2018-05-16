package org.resolvetosavelives.red.newentry.search

import java.util.UUID

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val mobileNumber: String? = null
) {

  fun toPatient(patientId: UUID, dateConverter: (String) -> Long): Patient {
    return Patient(
        uuid = patientId.toString(),
        fullName = personalDetails!!.fullName,
        dateOfBirth = dateConverter(personalDetails.dateOfBirth),
        ageWhenCreated = personalDetails.ageWhenCreated,
        gender = personalDetails.gender,
        mobileNumber = mobileNumber!!)
  }

  data class PersonalDetails(val fullName: String, val dateOfBirth: String, val ageWhenCreated: Int, val gender: Gender)
}
