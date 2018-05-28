package org.resolvetosavelives.red.newentry.search

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val address: Address? = null,
    val phoneNumbers: PhoneNumbers? = null,
    val bloodPressureMeasurements: BloodPressureMeasurement? = null
) {

  fun hasNullDateOfBirthAndAge(): Boolean {
    return personalDetails!!.dateOfBirth.isNullOrEmpty() && personalDetails.ageWhenCreated.isNullOrEmpty()
  }

  /**
   * [ageWhenCreated] is stored as a String instead of an Int because it's easy
   * to forget that [Int.toString] will return literal "null" for null Ints.
   */
  data class PersonalDetails(val fullName: String, val dateOfBirth: String?, val ageWhenCreated: String?, val gender: Gender?)

  data class PhoneNumbers(val primary: String, val secondary: String? = null)

  data class Address(val colonyOrVillage: String, val district: String, val state: String)

  // TODO: Persist to Patient
  data class BloodPressureMeasurement(val systolic: Int, val diastolic: Int)
}
