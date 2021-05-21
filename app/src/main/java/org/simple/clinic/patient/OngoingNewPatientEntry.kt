package org.simple.clinic.patient

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.simple.clinic.patient.PatientEntryValidationError.AgeExceedsMaxLimit
import org.simple.clinic.patient.PatientEntryValidationError.AgeExceedsMinLimit
import org.simple.clinic.patient.PatientEntryValidationError.BothDateOfBirthAndAgeAbsent
import org.simple.clinic.patient.PatientEntryValidationError.BothDateOfBirthAndAgePresent
import org.simple.clinic.patient.PatientEntryValidationError.ColonyOrVillageEmpty
import org.simple.clinic.patient.PatientEntryValidationError.DateOfBirthInFuture
import org.simple.clinic.patient.PatientEntryValidationError.DistrictEmpty
import org.simple.clinic.patient.PatientEntryValidationError.DobExceedsMaxLimit
import org.simple.clinic.patient.PatientEntryValidationError.DobExceedsMinLimit
import org.simple.clinic.patient.PatientEntryValidationError.EmptyAddressDetails
import org.simple.clinic.patient.PatientEntryValidationError.FullNameEmpty
import org.simple.clinic.patient.PatientEntryValidationError.InvalidDateOfBirth
import org.simple.clinic.patient.PatientEntryValidationError.MissingGender
import org.simple.clinic.patient.PatientEntryValidationError.PersonalDetailsEmpty
import org.simple.clinic.patient.PatientEntryValidationError.PhoneNumberLengthTooLong
import org.simple.clinic.patient.PatientEntryValidationError.PhoneNumberLengthTooShort
import org.simple.clinic.patient.PatientEntryValidationError.PhoneNumberNonNullButBlank
import org.simple.clinic.patient.PatientEntryValidationError.StateEmpty
import org.simple.clinic.patient.PatientPhoneNumberType.Mobile
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.Blank
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooLong
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LengthTooShort
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.scanid.PatientPrefillInfo
import org.simple.clinic.util.Optional
import org.simple.clinic.util.toNullable
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMaxAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator.Result.Invalid.ExceedsMinAgeLimit
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid
import java.time.format.DateTimeFormatter

/**
 * Represents user input on the UI, which is why every field is a String.
 * Parsing of user input happens later when this data class is converted
 * into a Patient object in [PatientRepository.saveOngoingEntryAsPatient].
 */
@Parcelize
data class OngoingNewPatientEntry(
    val personalDetails: PersonalDetails? = null,
    val address: Address? = null,
    val phoneNumber: PhoneNumber? = null,
    val identifier: Identifier? = null,
    val alternativeId: Identifier? = null,
    val reminderConsent: ReminderConsent = Granted
) : Parcelable {

  companion object {
    fun default(): OngoingNewPatientEntry {
      return OngoingNewPatientEntry()
    }

    fun fromFullName(fullName: String): OngoingNewPatientEntry {
      return OngoingNewPatientEntry(
          personalDetails = PersonalDetails(
              fullName = fullName,
              dateOfBirth = null,
              age = null,
              gender = null
          )
      )
    }

    fun fromPhoneNumber(phoneNumber: String): OngoingNewPatientEntry {
      return OngoingNewPatientEntry(phoneNumber = PhoneNumber(number = phoneNumber))
    }
  }

  fun withIdentifier(identifier: Identifier): OngoingNewPatientEntry {
    return this.copy(identifier = identifier)
  }

  fun withFullName(fullName: String): OngoingNewPatientEntry =
      copy(personalDetails = personalDetailsOrBlank().withFullName(fullName))

  fun withGender(gender: Optional<Gender>): OngoingNewPatientEntry =
      copy(personalDetails = personalDetailsOrBlank().withGender(gender.toNullable()))

  fun withAge(age: String): OngoingNewPatientEntry =
      copy(personalDetails = personalDetailsOrBlank().withAge(age))

  fun withDateOfBirth(dateOfBirth: String): OngoingNewPatientEntry =
      copy(personalDetails = personalDetailsOrBlank().withDateOfBirth(dateOfBirth))

  fun withPhoneNumber(phoneNumber: String): OngoingNewPatientEntry =
      copy(phoneNumber = if (phoneNumber.isNotBlank()) PhoneNumber(phoneNumber) else null)

  fun withColonyOrVillage(colonyOrVillage: String): OngoingNewPatientEntry =
      copy(address = addressOrBlank().withColonyOrVillage(colonyOrVillage))

  fun withDistrict(district: String): OngoingNewPatientEntry =
      copy(address = addressOrBlank().withDistrict(district))

  fun withState(state: String): OngoingNewPatientEntry =
      copy(address = addressOrBlank().withState(state))

  fun withAddress(address: Address): OngoingNewPatientEntry =
      copy(address = address)

  fun withConsent(reminderConsent: ReminderConsent): OngoingNewPatientEntry =
      copy(reminderConsent = reminderConsent)

  fun withAlternativeId(identifier: Identifier): OngoingNewPatientEntry =
      copy(alternativeId = identifier)

  fun withStreetAddress(streetAddress: String): OngoingNewPatientEntry =
      copy(address = addressOrBlank().copy(streetAddress = streetAddress))

  fun withZone(zone: String): OngoingNewPatientEntry =
      copy(address = addressOrBlank().copy(zone = zone))

  fun withPatientPrefillInfo(patientProfileInfo: PatientPrefillInfo, identifier: Identifier): OngoingNewPatientEntry =
      copy(
          personalDetails = PersonalDetails(
              fullName = patientProfileInfo.fullName,
              dateOfBirth = patientProfileInfo.dateOfBirth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
              gender = Gender.Unknown(patientProfileInfo.gender),
              age = null),
          address = addressOrBlank().withColonyOrVillage(patientProfileInfo.address),
          identifier = identifier)


  fun validationErrors(
      dobValidator: UserInputDateValidator,
      numberValidator: PhoneNumberValidator,
      ageValidator: UserInputAgeValidator
  ): List<PatientEntryValidationError> {
    val errors = ArrayList<PatientEntryValidationError>()

    if (personalDetails == null) {
      errors += PersonalDetailsEmpty
    }

    personalDetails?.apply {
      if (dateOfBirth.isNullOrBlank() && age.isNullOrBlank()) {
        errors += BothDateOfBirthAndAgeAbsent

      } else if (dateOfBirth?.isNotBlank() == true && age?.isNotBlank() == true) {
        errors += BothDateOfBirthAndAgePresent

      } else if (dateOfBirth != null) {
        errors += dobValidationCheck(dobValidator, dateOfBirth, ageValidator)

      } else if (age != null) {
        validateAge(errors, ageValidator, age)
      }

      if (fullName.isBlank()) {
        errors += FullNameEmpty
      }
      if (gender == null) {
        errors += MissingGender
      }
    }

    if (phoneNumber != null) {
      validatePhoneNumber(errors, numberValidator, phoneNumber)
    }

    if (address == null) {
      errors += EmptyAddressDetails
    }

    address?.apply {
      if (colonyOrVillage.isBlank()) {
        errors += ColonyOrVillageEmpty
      }
      if (district.isBlank()) {
        errors += DistrictEmpty
      }
      if (state.isBlank()) {
        errors += StateEmpty
      }
    }

    return errors
  }

  private fun validatePhoneNumber(
      errors: ArrayList<PatientEntryValidationError>,
      numberValidator: PhoneNumberValidator,
      phoneNumber: PhoneNumber
  ) {
    errors += when (val errorNumber = numberValidator.validate(phoneNumber.number, LANDLINE_OR_MOBILE)) {
      is Blank -> listOf(PhoneNumberNonNullButBlank)
      is LengthTooShort -> listOf(PhoneNumberLengthTooShort(errorNumber.minimumAllowedNumberLength))
      is LengthTooLong -> listOf(PhoneNumberLengthTooLong(errorNumber.maximumRequiredNumberLength))
      is PhoneNumberValidator.Result.ValidNumber -> listOf()
    }
  }

  private fun validateAge(
      errors: ArrayList<PatientEntryValidationError>,
      ageValidator: UserInputAgeValidator,
      age: String
  ) {
    errors += when (ageValidator.validate(age.toInt())) {
      ExceedsMaxAgeLimit -> listOf(AgeExceedsMaxLimit)
      ExceedsMinAgeLimit -> listOf(AgeExceedsMinLimit)
      else -> emptyList()
    }
  }

  private fun dobValidationCheck(
      dobValidator: UserInputDateValidator,
      dateOfBirth: String,
      ageValidator: UserInputAgeValidator
  ): List<PatientEntryValidationError> {
    return when (dobValidator.validate(dateOfBirth)) {
      InvalidPattern -> listOf(InvalidDateOfBirth)
      DateIsInFuture -> listOf(DateOfBirthInFuture)
      is Valid -> validateDob(ageValidator, dateOfBirth)
    }
  }

  private fun validateDob(
      ageValidator: UserInputAgeValidator,
      dateOfBirth: String
  ): List<PatientEntryValidationError> {
    return when (ageValidator.validate(dateOfBirth)) {
      ExceedsMaxAgeLimit -> listOf(DobExceedsMaxLimit)
      ExceedsMinAgeLimit -> listOf(DobExceedsMinLimit)
      else -> emptyList()
    }
  }

  private fun personalDetailsOrBlank(): PersonalDetails =
      personalDetails ?: PersonalDetails.BLANK

  private fun addressOrBlank(): Address =
      address ?: Address.BLANK

  /**
   * [age] is stored as a String instead of an Int because it's easy
   * to forget that [Int.toString] will return literal "null" for null Ints.
   */
  @Parcelize
  data class PersonalDetails(
      val fullName: String,
      val dateOfBirth: String?,
      val age: String?,
      val gender: Gender?
  ) : Parcelable {
    companion object {
      val BLANK = PersonalDetails("", null, null, null)
    }

    fun withFullName(fullName: String): PersonalDetails =
        copy(fullName = fullName)

    fun withDateOfBirth(dateOfBirth: String): PersonalDetails =
        copy(dateOfBirth = if (dateOfBirth.isBlank()) null else dateOfBirth)

    fun withAge(age: String): PersonalDetails =
        copy(age = if (age.isBlank()) null else age)

    fun withGender(gender: Gender?): PersonalDetails =
        copy(gender = gender)
  }

  @Parcelize
  data class PhoneNumber(
      val number: String,
      val type: PatientPhoneNumberType = Mobile,
      val active: Boolean = true
  ) : Parcelable

  @Parcelize
  data class Address(
      val colonyOrVillage: String,
      val district: String,
      val state: String,
      val streetAddress: String?,
      val zone: String?
  ) : Parcelable {
    companion object {
      val BLANK = Address("", "", "", "", "")

      fun withDistrictAndState(district: String, state: String): Address =
          Address("", district, state, "", "")
    }

    fun withColonyOrVillage(colonyOrVillage: String): Address =
        copy(colonyOrVillage = colonyOrVillage)

    fun withDistrict(district: String): Address =
        copy(district = district)

    fun withState(state: String): Address =
        copy(state = state)
  }
}
