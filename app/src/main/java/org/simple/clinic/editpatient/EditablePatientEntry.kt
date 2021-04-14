package org.simple.clinic.editpatient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.editpatient.EditPatientValidationError.AgeExceedsMaxLimit
import org.simple.clinic.editpatient.EditPatientValidationError.AgeExceedsMinLimit
import org.simple.clinic.editpatient.EditPatientValidationError.BothDateOfBirthAndAgeAdsent
import org.simple.clinic.editpatient.EditPatientValidationError.ColonyOrVillageEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthExceedsMaxLimit
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthExceedsMinLimit
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthInFuture
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthParseError
import org.simple.clinic.editpatient.EditPatientValidationError.DistrictEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.FullNameEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooLong
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooShort
import org.simple.clinic.editpatient.EditPatientValidationError.StateEmpty
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithAge
import org.simple.clinic.editpatient.EditablePatientEntry.EitherAgeOrDateOfBirth.EntryWithDateOfBirth
import org.simple.clinic.patient.Age
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.Patient
import org.simple.clinic.patient.PatientAddress
import org.simple.clinic.patient.PatientPhoneNumber
import org.simple.clinic.patient.businessid.BusinessId
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type
import org.simple.clinic.util.valueOrEmpty
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMaxAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMinAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

typealias ValidationCheck = () -> EditPatientValidationError?

@Parcelize
data class EditablePatientEntry @Deprecated("Use the `from` factory function instead.") constructor(
    val patientUuid: UUID,
    val name: String,
    val gender: Gender,
    val phoneNumber: String,
    val colonyOrVillage: String,
    val district: String,
    val state: String,
    val ageOrDateOfBirth: EitherAgeOrDateOfBirth,
    val zone: String,
    val streetAddress: String,
    val alternativeId: String
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
        dateOfBirthFormatter: DateTimeFormatter,
        alternativeId: BusinessId?,
        saveButtonState: EditPatientState?
    ): EditablePatientEntry {
      return EditablePatientEntry(
          patientUuid = patient.uuid,
          name = patient.fullName,
          gender = patient.gender,
          phoneNumber = phoneNumber?.number.valueOrEmpty(),
          colonyOrVillage = address.colonyOrVillage.valueOrEmpty(),
          district = address.district,
          state = address.state,
          ageOrDateOfBirth = ageOrDateOfBirth(patient.age, patient.dateOfBirth, dateOfBirthFormatter),
          zone = address.zone.valueOrEmpty(),
          streetAddress = address.streetAddress.valueOrEmpty(),
          alternativeId = alternativeId?.identifier?.value.valueOrEmpty()
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

  fun updateName(name: String): EditablePatientEntry =
      copy(name = name)

  fun updateGender(gender: Gender): EditablePatientEntry =
      copy(gender = gender)

  fun updatePhoneNumber(phoneNumber: String): EditablePatientEntry =
      copy(phoneNumber = phoneNumber)

  fun updateColonyOrVillage(colonyOrVillage: String): EditablePatientEntry =
      copy(colonyOrVillage = colonyOrVillage)

  fun updateDistrict(district: String): EditablePatientEntry =
      copy(district = district)

  fun updateState(state: String): EditablePatientEntry =
      copy(state = state)

  fun updateAge(age: String): EditablePatientEntry =
      copy(ageOrDateOfBirth = EntryWithAge(age))

  fun updateDateOfBirth(dateOfBirth: String): EditablePatientEntry =
      copy(ageOrDateOfBirth = EntryWithDateOfBirth(dateOfBirth))

  fun updateZone(zone: String): EditablePatientEntry =
      copy(zone = zone)

  fun updateStreetAddress(streetAddress: String): EditablePatientEntry =
      copy(streetAddress = streetAddress)

  fun updateAlternativeId(alternativeId: String): EditablePatientEntry =
      copy(alternativeId = alternativeId)

  fun validate(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator,
      dobValidator: UserInputDateValidator,
      ageValidator: UserInputAgeValidator
  ): Set<EditPatientValidationError> {
    return getValidationChecks(alreadySavedNumber, numberValidator, dobValidator, ageValidator)
        .mapNotNull { it.invoke() }
        .toSet()
  }

  private fun getValidationChecks(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator,
      dobValidator: UserInputDateValidator,
      ageValidator: UserInputAgeValidator
  ): List<ValidationCheck> {
    return listOf(
        nameCheck(),
        phoneNumberCheck(alreadySavedNumber, numberValidator),
        colonyOrVillageCheck(),
        stateCheck(),
        districtCheck(),
        ageOrDateOfBirthCheck(dobValidator, ageValidator)
    )
  }

  private fun nameCheck(): ValidationCheck =
      { if (name.isBlank()) FullNameEmpty else null }

  private fun phoneNumberCheck(
      alreadySavedNumber: PatientPhoneNumber?,
      numberValidator: PhoneNumberValidator
  ): ValidationCheck = {
    when (val error = numberValidator.validate(phoneNumber, Type.LANDLINE_OR_MOBILE)) {
      is LengthTooShort -> PhoneNumberLengthTooShort(error.minimumAllowedNumberLength)
      is LengthTooLong -> PhoneNumberLengthTooLong(error.maximumRequiredNumberLength)
      is Blank -> checkIfPhoneNumberIsBlank(alreadySavedNumber)
      is PhoneNumberValidator.Result.ValidNumber -> null
    }
  }

  private fun checkIfPhoneNumberIsBlank(alreadySavedNumber: PatientPhoneNumber?) =
      if (alreadySavedNumber != null) PhoneNumberEmpty else null

  private fun colonyOrVillageCheck(): ValidationCheck =
      { if (colonyOrVillage.isBlank()) ColonyOrVillageEmpty else null }

  private fun stateCheck(): ValidationCheck =
      { if (state.isBlank()) StateEmpty else null }

  private fun districtCheck(): ValidationCheck =
      { if (district.isBlank()) DistrictEmpty else null }

  private fun ageOrDateOfBirthCheck(
      dobValidator: UserInputDateValidator,
      ageValidator: UserInputAgeValidator
  ): ValidationCheck = {
    when (ageOrDateOfBirth) {
      is EntryWithAge -> ageCheck(ageOrDateOfBirth, ageValidator)
      is EntryWithDateOfBirth -> dateOfBirthCheck(dobValidator, ageOrDateOfBirth, ageValidator)
    }
  }

  private fun dateOfBirthCheck(
      dobValidator: UserInputDateValidator,
      ageOrDateOfBirth: EntryWithDateOfBirth,
      ageValidator: UserInputAgeValidator
  ): EditPatientValidationError? {
    val dateOfBirth = ageOrDateOfBirth.dateOfBirth

    return when (dobValidator.validate(dateOfBirth)) {
      InvalidPattern -> DateOfBirthParseError
      DateIsInFuture -> DateOfBirthInFuture
      is UserInputDateValidator.Result.Valid -> validateDob(ageValidator, dateOfBirth)
    }
  }

  private fun validateDob(
      ageValidator: UserInputAgeValidator,
      dateOfBirth: String
  ): EditPatientValidationError? {
    return when (ageValidator.validate(dateOfBirth)) {
      ExceedsMaxAgeLimit -> DateOfBirthExceedsMaxLimit
      ExceedsMinAgeLimit -> DateOfBirthExceedsMinLimit
      else -> null
    }
  }

  private fun ageCheck(
      ageOrDateOfBirth: EntryWithAge,
      ageValidator: UserInputAgeValidator
  ): EditPatientValidationError? {
    val age = ageOrDateOfBirth.age

    return when {
      age.isBlank() -> BothDateOfBirthAndAgeAdsent
      else -> ageValidate(ageValidator, age)
    }
  }

  private fun ageValidate(ageValidator: UserInputAgeValidator, age: String): EditPatientValidationError? {
    return when (ageValidator.validate(age.toInt())) {
      ExceedsMaxAgeLimit -> AgeExceedsMaxLimit
      ExceedsMinAgeLimit -> AgeExceedsMinLimit
      else -> null
    }
  }
}
