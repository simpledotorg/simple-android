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
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.BLANK
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.VALID
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type
import org.simple.clinic.util.valueOrEmpty
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.UUID

typealias ValidationCheck = () -> EditPatientValidationError?

@Parcelize
data class OngoingEditPatientEntry @Deprecated("Use the `from` factory function instead.") constructor(
    val patientUuid: UUID,
    val name: String,
    val gender: Gender,
    val phoneNumber: String,
    val colonyOrVillage: String,
    val district: String,
    val state: String,
    val ageOrDateOfBirth: EitherAgeOrDateOfBirth
) : Parcelable {

  sealed class EitherAgeOrDateOfBirth : Parcelable {
    abstract val isBlank: Boolean

    @Parcelize
    data class EntryWithAge(val age: String) : EitherAgeOrDateOfBirth() {
      override val isBlank: Boolean
        get() = age.isBlank()
    }

    @Parcelize
    data class EntryWithDateOfBirth(val dateOfBirth: String) : EitherAgeOrDateOfBirth() {
      override val isBlank: Boolean
        get() = dateOfBirth.isBlank()
    }
  }

  companion object {
    fun from(
        patient: Patient,
        address: PatientAddress,
        phoneNumber: PatientPhoneNumber?,
        dateOfBirthFormatter: DateTimeFormatter
    ): OngoingEditPatientEntry {
      return OngoingEditPatientEntry(
          patientUuid = patient.uuid,
          name = patient.fullName,
          gender = patient.gender,
          phoneNumber = phoneNumber?.number.valueOrEmpty(),
          colonyOrVillage = address.colonyOrVillage.valueOrEmpty(),
          district = address.district,
          state = address.state,
          ageOrDateOfBirth = ageOrDateOfBirth(patient.age, patient.dateOfBirth, dateOfBirthFormatter)
      )
    }

    private fun ageOrDateOfBirth(
        age: Age?,
        dateOfBirth: LocalDate?,
        dateOfBirthFormatter: DateTimeFormatter
    ): EitherAgeOrDateOfBirth {
      return when {
        dateOfBirth != null -> EntryWithDateOfBirth(dateOfBirth.format(dateOfBirthFormatter))
        age != null -> EntryWithAge(age.value.toString())
        else -> throw IllegalStateException("`age` or `dateOfBirth` should be present")
      }
    }
  }

  fun updateName(name: String): OngoingEditPatientEntry =
      copy(name = name)

  fun updateGender(gender: Gender): OngoingEditPatientEntry =
      copy(gender = gender)

  fun updatePhoneNumber(phoneNumber: String): OngoingEditPatientEntry =
      copy(phoneNumber = phoneNumber)

  fun updateColonyOrVillage(colonyOrVillage: String): OngoingEditPatientEntry =
      copy(colonyOrVillage = colonyOrVillage)

  fun updateDistrict(district: String): OngoingEditPatientEntry =
      copy(district = district)

  fun updateState(state: String): OngoingEditPatientEntry =
      copy(state = state)

  fun updateAge(age: String): OngoingEditPatientEntry =
      copy(ageOrDateOfBirth = EntryWithAge(age))

  fun updateDateOfBirth(dateOfBirth: String): OngoingEditPatientEntry =
      copy(ageOrDateOfBirth = EntryWithDateOfBirth(dateOfBirth))

  fun validate(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator,
      dobValidator: UserInputDateValidator
  ): Set<EditPatientValidationError> {
    return getValidationChecks(alreadySavedNumber, numberValidator, ageOrDateOfBirth, dobValidator)
        .mapNotNull { check -> check() }
        .toSet()
  }

  private fun getValidationChecks(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator,
      ageOrDateOfBirth: EitherAgeOrDateOfBirth,
      dobValidator: UserInputDateValidator
  ): List<ValidationCheck> {
    return listOf(
        nameCheck(),
        phoneNumberCheck(alreadySavedNumber, numberValidator),
        colonyOrVillageCheck(),
        stateCheck(),
        districtCheck(),
        ageOrDateOfBirthCheck(ageOrDateOfBirth, dobValidator)
    )
  }

  private fun nameCheck(): ValidationCheck =
      { if (name.isBlank()) FULL_NAME_EMPTY else null }

  private fun phoneNumberCheck(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator
  ): ValidationCheck = {
    when (numberValidator.validate(phoneNumber, Type.LANDLINE_OR_MOBILE)) {
      LENGTH_TOO_SHORT -> PHONE_NUMBER_LENGTH_TOO_SHORT
      LENGTH_TOO_LONG -> PHONE_NUMBER_LENGTH_TOO_LONG
      BLANK -> if (alreadySavedNumber != null) PHONE_NUMBER_EMPTY else null
      VALID -> null
    }
  }

  private fun colonyOrVillageCheck(): ValidationCheck =
      { if (colonyOrVillage.isBlank()) COLONY_OR_VILLAGE_EMPTY else null }

  private fun stateCheck(): ValidationCheck =
      { if (state.isBlank()) STATE_EMPTY else null }

  private fun districtCheck(): ValidationCheck =
      { if (district.isBlank()) DISTRICT_EMPTY else null }

  private fun ageOrDateOfBirthCheck(
      ageOrDateOfBirth: EitherAgeOrDateOfBirth,
      dobValidator: UserInputDateValidator
  ): ValidationCheck = {
    when (ageOrDateOfBirth) {
      is EntryWithAge -> if (ageOrDateOfBirth.age.isBlank()) BOTH_DATEOFBIRTH_AND_AGE_ABSENT else null

      is EntryWithDateOfBirth -> {
        when (dobValidator.validate(ageOrDateOfBirth.dateOfBirth)) {
          InvalidPattern -> INVALID_DATE_OF_BIRTH
          DateIsInFuture -> DATE_OF_BIRTH_IN_FUTURE
          is Valid -> null
        }
      }
    }
  }
}
