package org.resolvetosavelives.red.patient

/**
 * Represents user input on the UI, which is why every field is a String.
 * Parsing of user input happens later when this data class is converted
 * into a Patient object in [PatientRepository.saveOngoingEntryAsPatient].
 */
data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null
) {

  fun hasNullDateOfBirthAndAge(): Boolean {
    return personalDetails!!.dateOfBirth.isNullOrEmpty() && personalDetails.age.isNullOrEmpty()
  }

  /**
   * [age] is stored as a String instead of an Int because it's easy
   * to forget that [Int.toString] will return literal "null" for null Ints.
   */
  data class PersonalDetails(val fullName: String, val dateOfBirth: String?, val age: String?, val gender: Gender?)

  data class PhoneNumber(val number: String, val type: PatientPhoneNumberType = PatientPhoneNumberType.MOBILE, val active: Boolean = true)

  data class Address(val colonyOrVillage: String, val district: String, val state: String)
}
