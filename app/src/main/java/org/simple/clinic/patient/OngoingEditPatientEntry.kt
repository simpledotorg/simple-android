package org.simple.clinic.patient

import org.simple.clinic.ageanddateofbirth.DateOfBirthFormatValidator
import org.simple.clinic.editpatient.PatientEditValidationError
import org.simple.clinic.editpatient.PatientEditValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type

data class OngoingEditPatientEntry(
    val name: String,
    val gender: Gender,
    val phoneNumber: String,
    val colonyOrVillage: String,
    val district: String,
    val state: String,
    val ageOrDateOfBirth: EitherAgeOrDateOfBirth
) {
  fun validate(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator,
      dateOfBirthFormatValidator: DateOfBirthFormatValidator
  ): Set<PatientEditValidationError> {
    val errors = mutableSetOf<PatientEditValidationError>()

    if (name.isBlank()) {
      errors.add(FULL_NAME_EMPTY)
    }

    when (numberValidator.validate(phoneNumber, Type.LANDLINE_OR_MOBILE)) {
      Result.LENGTH_TOO_SHORT -> errors.add(PHONE_NUMBER_LENGTH_TOO_SHORT)
      Result.LENGTH_TOO_LONG -> errors.add(PHONE_NUMBER_LENGTH_TOO_LONG)
      Result.BLANK -> {
        if (alreadySavedNumber != null) {
          errors.add(PHONE_NUMBER_EMPTY)
        }
      }

      Result.VALID -> {
        // Do nothing here
      }
    }

    if (colonyOrVillage.isBlank()) {
      errors.add(COLONY_OR_VILLAGE_EMPTY)
    }

    if (state.isBlank()) {
      errors.add(STATE_EMPTY)
    }

    if (district.isBlank()) {
      errors.add(DISTRICT_EMPTY)
    }

    if (ageOrDateOfBirth is EitherAgeOrDateOfBirth.EntryWithDateOfBirth) {
      val validationResult = dateOfBirthFormatValidator.validate(ageOrDateOfBirth.dateOfBirth)

      when (validationResult) {
        DateOfBirthFormatValidator.Result.INVALID_PATTERN -> {
          errors.add(INVALID_DATE_OF_BIRTH)
        }
        DateOfBirthFormatValidator.Result.DATE_IS_IN_FUTURE -> {
          errors.add(DATE_OF_BIRTH_IN_FUTURE)
        }
        DateOfBirthFormatValidator.Result.VALID -> {
          // Nothing to do here.
        }
      }

    } else if(ageOrDateOfBirth is EitherAgeOrDateOfBirth.EntryWithAge) {
      if (ageOrDateOfBirth.age.isBlank()) {
        errors.add(BOTH_DATEOFBIRTH_AND_AGE_ABSENT)
      }
    }

    return errors
  }

  sealed class EitherAgeOrDateOfBirth {

    data class EntryWithAge(val age: String): EitherAgeOrDateOfBirth()

    data class EntryWithDateOfBirth(val dateOfBirth: String): EitherAgeOrDateOfBirth()
  }
}
