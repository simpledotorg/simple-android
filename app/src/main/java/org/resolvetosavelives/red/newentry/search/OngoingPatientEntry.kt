package org.resolvetosavelives.red.newentry.search

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val mobileNumbers: MobileNumbers? = null,
    val address: Address? = null,
    val bloodPressureMeasurements: BloodPressureMeasurement? = null
) {

  data class PersonalDetails(val fullName: String, val dateOfBirth: String, val ageWhenCreated: String?, val gender: Gender?)

  data class MobileNumbers(val primary: String, val secondary: String? = null)

  // TODO: Persist to Patient
  data class Address(val colonyOrVillage: String, val district: String, val state: String)

  // TODO: Persist to Patient
  data class BloodPressureMeasurement(val systolic: Int, val diastolic: Int)
}
