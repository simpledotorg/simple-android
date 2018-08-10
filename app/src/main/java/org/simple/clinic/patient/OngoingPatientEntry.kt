package org.simple.clinic.patient

import org.simple.clinic.newentry.DateOfBirthFormatValidator
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.DATE_IS_IN_FUTURE
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.INVALID_PATTERN
import org.simple.clinic.newentry.DateOfBirthFormatValidator.Result.VALID
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_PRESENT
import org.simple.clinic.patient.PatientEntryValidationError.COLONY_OR_VILLAGE_NON_NULL_BUT_BLANK
import org.simple.clinic.patient.PatientEntryValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.patient.PatientEntryValidationError.DISTRICT_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.EMPTY_ADDRESS_DETAILS
import org.simple.clinic.patient.PatientEntryValidationError.FULL_NAME_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.patient.PatientEntryValidationError.MISSING_GENDER
import org.simple.clinic.patient.PatientEntryValidationError.PERSONAL_DETAILS_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_NON_NULL_BUT_BLANK
import org.simple.clinic.patient.PatientEntryValidationError.STATE_EMPTY

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

  fun validationErrors(dobValidator: DateOfBirthFormatValidator): ArrayList<PatientEntryValidationError> {
    val errors = ArrayList<PatientEntryValidationError>()

    if (personalDetails == null) {
      errors += PERSONAL_DETAILS_EMPTY
    }

    personalDetails?.apply {
      if (dateOfBirth.isNullOrBlank() && age.isNullOrBlank()) {
        errors += BOTH_DATEOFBIRTH_AND_AGE_ABSENT

      } else if (dateOfBirth?.isNotBlank() == true && age?.isNotBlank() == true) {
        errors += BOTH_DATEOFBIRTH_AND_AGE_PRESENT

      } else if (dateOfBirth != null) {
        val dobValidationResult = dobValidator.validate(dateOfBirth)
        errors += when (dobValidationResult) {
          INVALID_PATTERN -> listOf(INVALID_DATE_OF_BIRTH)
          DATE_IS_IN_FUTURE -> listOf(DATE_OF_BIRTH_IN_FUTURE)
          VALID -> listOf()
        }
      }
      if (fullName.isBlank()) {
        errors += FULL_NAME_EMPTY
      }
      if (gender == null) {
        errors += MISSING_GENDER
      }
    }

    if (phoneNumber != null && phoneNumber.number.isBlank()) {
      errors += PHONE_NUMBER_NON_NULL_BUT_BLANK
    }

    if (address == null) {
      errors += EMPTY_ADDRESS_DETAILS
    }

    address?.apply {
      if (colonyOrVillage != null && colonyOrVillage.isBlank()) {
        errors += COLONY_OR_VILLAGE_NON_NULL_BUT_BLANK
      }
      if (district.isBlank()) {
        errors += DISTRICT_EMPTY
      }
      if (state.isBlank()) {
        errors += STATE_EMPTY
      }
    }

    return errors
  }

  /**
   * [age] is stored as a String instead of an Int because it's easy
   * to forget that [Int.toString] will return literal "null" for null Ints.
   */
  data class PersonalDetails(val fullName: String, val dateOfBirth: String?, val age: String?, val gender: Gender?)

  data class PhoneNumber(val number: String, val type: PatientPhoneNumberType = PatientPhoneNumberType.MOBILE, val active: Boolean = true)

  data class Address(val colonyOrVillage: String?, val district: String, val state: String)
}
