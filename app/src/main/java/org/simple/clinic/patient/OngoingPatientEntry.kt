package org.simple.clinic.patient

import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.DATE_IS_IN_FUTURE
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.INVALID_PATTERN
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.VALID

/**
 * Represents loggedInUser input on the UI, which is why every field is a String.
 * Parsing of loggedInUser input happens later when this data class is converted
 * into a Patient object in [PatientRepository.saveOngoingEntryAsPatient].
 */
data class OngoingPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null
) {

  enum class ValidationError {
    PERSONAL_DETAILS_EMPTY,
    FULL_NAME_EMPTY,
    BOTH_DATEOFBIRTH_AND_AGE_ABSENT,
    BOTH_DATEOFBIRTH_AND_AGE_PRESENT,
    INVALID_DATE_OF_BIRTH,
    DATE_OF_BIRTH_IS_IN_FUTURE,
    MISSING_GENDER,

    PHONE_NUMBER_NON_NULL_BUT_BLANK,

    EMPTY_ADDRESS_DETAILS,
    COLONY_OR_VILLAGE_NON_NULL_BUT_BLANK,
    DISTRICT_EMPTY,
    STATE_EMPTY
  }

  fun validationErrors(dobValidator: DateOfBirthFormatValidator): ArrayList<ValidationError> {
    val errors = ArrayList<ValidationError>()

    if (personalDetails == null) {
      errors += ValidationError.PERSONAL_DETAILS_EMPTY
    }

    personalDetails?.apply {
      if (dateOfBirth.isNullOrBlank() && age.isNullOrBlank()) {
        errors += ValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT

      } else if (dateOfBirth?.isNotBlank() == true && age?.isNotBlank() == true) {
        errors += ValidationError.BOTH_DATEOFBIRTH_AND_AGE_PRESENT

      } else if (dateOfBirth != null) {
        val dobValidationResult = dobValidator.validate(dateOfBirth)
        errors += when (dobValidationResult) {
          INVALID_PATTERN -> listOf(ValidationError.INVALID_DATE_OF_BIRTH)
          DATE_IS_IN_FUTURE -> listOf(ValidationError.DATE_OF_BIRTH_IS_IN_FUTURE)
          VALID -> listOf()
        }
      }
      if (fullName.isBlank()) {
        errors += ValidationError.FULL_NAME_EMPTY
      }
      if (gender == null) {
        errors += ValidationError.MISSING_GENDER
      }
    }

    if (phoneNumber != null && phoneNumber.number.isBlank()) {
      errors += ValidationError.PHONE_NUMBER_NON_NULL_BUT_BLANK
    }

    if (address == null) {
      errors += ValidationError.EMPTY_ADDRESS_DETAILS
    }

    address?.apply {
      if (colonyOrVillage != null && colonyOrVillage.isBlank()) {
        errors += ValidationError.COLONY_OR_VILLAGE_NON_NULL_BUT_BLANK
      }
      if (district.isBlank()) {
        errors += ValidationError.DISTRICT_EMPTY
      }
      if (state.isBlank()) {
        errors += ValidationError.STATE_EMPTY
      }
    }

    return errors
  }

  sealed class ValidationResult {
    class Valid : ValidationResult()
    data class Invalid(val error: Throwable) : ValidationResult()
  }

  /**
   * [age] is stored as a String instead of an Int because it's easy
   * to forget that [Int.toString] will return literal "null" for null Ints.
   */
  data class PersonalDetails(val fullName: String, val dateOfBirth: String?, val age: String?, val gender: Gender?)

  data class PhoneNumber(val number: String, val type: PatientPhoneNumberType = PatientPhoneNumberType.MOBILE, val active: Boolean = true)

  data class Address(val colonyOrVillage: String?, val district: String, val state: String)
}
