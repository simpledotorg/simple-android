package org.resolvetosavelives.red.patient

data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null
) {

  fun hasNullDateOfBirthAndAge(): Boolean {
    return personalDetails!!.dateOfBirth.isNullOrEmpty() && personalDetails.ageWhenCreated.isNullOrEmpty()
  }

  /**
   * [ageWhenCreated] is stored as a String instead of an Int because it's easy
   * to forget that [Int.toString] will return literal "null" for null Ints.
   */
  // TODO: Rename to 'age'.
  data class PersonalDetails(val fullName: String, val dateOfBirth: String?, val ageWhenCreated: String?, val gender: Gender?)

  data class PhoneNumber(val number: String, val type: PatientPhoneNumberType = PatientPhoneNumberType.MOBILE, val active: Boolean = true)

  data class Address(val colonyOrVillage: String, val district: String, val state: String)
}
