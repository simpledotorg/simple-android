package org.simple.clinic.editpatient

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.editpatient.EditPatientValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.editpatient.EditPatientValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.editpatient.EditPatientValidationError.DISTRICT_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.FULL_NAME_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_EMPTY
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.editpatient.EditPatientValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.editpatient.EditPatientValidationError.STATE_EMPTY
import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.OngoingEditPatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

@Parcelize
data class OngoingEditPatientEntry( // TODO(rj) 23/Sep/19 - Don't expose the constructor
    val patientUuid: UUID,
    val name: String,
    val gender: Gender,
    val phoneNumber: String,
    val colonyOrVillage: String,
    val district: String,
    val state: String,
    val ageOrDateOfBirth: EitherAgeOrDateOfBirth
) : Parcelable {
  companion object {
    fun from(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?,
        dateOfBirthFormatter: DateTimeFormatter
    ): OngoingEditPatientEntry {
      val ageOrDateOfBirth = when {
        patient.age != null -> EntryWithAge(patient.age.value.toString())
        patient.dateOfBirth != null -> EntryWithDateOfBirth(patient.dateOfBirth.format(dateOfBirthFormatter))
        else -> throw IllegalStateException("`age` or `dateOfBirth` should be present")
      }

      return OngoingEditPatientEntry(
          patientUuid = patient.uuid,
          name = patient.fullName,
          gender = patient.gender,
          phoneNumber = phoneNumber?.number ?: "",
          colonyOrVillage = address.colonyOrVillage ?: "",
          district = address.district,
          state = address.state,
          ageOrDateOfBirth = ageOrDateOfBirth
      )
    }
  }

  fun validate(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator,
      dobValidator: UserInputDateValidator
  ): Set<EditPatientValidationError> {
    val errors = mutableSetOf<EditPatientValidationError>()

    if (name.isBlank()) {
      errors.add(FULL_NAME_EMPTY)
    }

    when (numberValidator.validate(phoneNumber, Type.LANDLINE_OR_MOBILE)) {
      Result.LENGTH_TOO_SHORT -> errors.add(PHONE_NUMBER_LENGTH_TOO_SHORT)
      Result.LENGTH_TOO_LONG -> errors.add(PHONE_NUMBER_LENGTH_TOO_LONG)
      Result.BLANK -> alreadySavedNumber?.let { errors.add(PHONE_NUMBER_EMPTY) }

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

    if (ageOrDateOfBirth is EntryWithDateOfBirth) {
      when (dobValidator.validate(ageOrDateOfBirth.dateOfBirth)) {
        InvalidPattern -> errors.add(INVALID_DATE_OF_BIRTH)
        DateIsInFuture -> errors.add(DATE_OF_BIRTH_IN_FUTURE)
        is Valid -> { /* Nothing to do here. */ }
      }

    } else if (ageOrDateOfBirth is EntryWithAge) {
      if (ageOrDateOfBirth.age.isBlank()) {
        errors.add(BOTH_DATEOFBIRTH_AND_AGE_ABSENT)
      }
    }

    return errors
  }

  sealed class EitherAgeOrDateOfBirth : Parcelable {

    @Parcelize
    data class EntryWithAge(val age: String) : EitherAgeOrDateOfBirth()

    @Parcelize
    data class EntryWithDateOfBirth(val dateOfBirth: String) : EitherAgeOrDateOfBirth()
  }
}
