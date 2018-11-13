package org.simple.clinic.editpatient

import org.simple.clinic.editpatient.PatientEditValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.PatientEditValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.PatientEditValidationError.STATE_EMPTY
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type

data class OngoingEditPatientEntry(
    val name: String,
    val gender: Gender,
    val phoneNumber: String,
    val colonyOrVillage: String,
    val district: String,
    val state: String
) {
  fun validate(alreadySavedNumber: PatientPhoneNumber?, numberValidator: PhoneNumberValidator): Set<PatientEditValidationError> {
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

    return errors
  }
}
