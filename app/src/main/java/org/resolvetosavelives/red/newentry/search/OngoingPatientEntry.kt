package org.resolvetosavelives.red.newentry.search

import java.util.UUID

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val mobileNumbers: MobileNumbers? = null
) {

  fun toPatient(patientId: UUID, dateConverter: (String) -> Long): Patient {
    return Patient(
        uuid = patientId.toString(),
        fullName = personalDetails!!.fullName,
        dateOfBirth = dateConverter(personalDetails.dateOfBirth),
        ageWhenCreated = personalDetails.ageWhenCreated,
        gender = personalDetails.gender,
        mobileNumber = mobileNumbers!!.primary)
  }

  data class PersonalDetails(val fullName: String, val dateOfBirth: String, val ageWhenCreated: Int, val gender: Gender)

  data class MobileNumbers(val primary: String, val secondary: String? = null)
}
