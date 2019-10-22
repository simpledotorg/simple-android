package org.simple.clinic.patient

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_PRESENT
import org.simple.clinic.patient.PatientEntryValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.patient.PatientEntryValidationError.DISTRICT_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.EMPTY_ADDRESS_DETAILS
import org.simple.clinic.patient.PatientEntryValidationError.FULL_NAME_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.patient.PatientEntryValidationError.MISSING_GENDER
import org.simple.clinic.patient.PatientEntryValidationError.PERSONAL_DETAILS_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_NON_NULL_BUT_BLANK
import org.simple.clinic.patient.PatientEntryValidationError.STATE_EMPTY
import org.simple.clinic.patient.PatientPhoneNumberType.Mobile
import org.simple.clinic.patient.ReminderConsent.Granted
import org.simple.clinic.patient.businessid.Identifier
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.BLANK
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_LONG
import org.simple.clinic.registration.phone.PhoneNumberValidator.Result.LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator.Type.LANDLINE_OR_MOBILE
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.DateIsInFuture
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Invalid.InvalidPattern
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator.Result.Valid

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
    val reminderConsent: ReminderConsent = Granted
) : Parcelable {

  companion object {
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

  fun validationErrors(dobValidator: UserInputDateValidator, numberValidator: PhoneNumberValidator): List<PatientEntryValidationError> {
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
          InvalidPattern -> listOf(INVALID_DATE_OF_BIRTH)
          DateIsInFuture -> listOf(DATE_OF_BIRTH_IN_FUTURE)
          is Valid -> emptyList()
        }
      }
      if (fullName.isBlank()) {
        errors += FULL_NAME_EMPTY
      }
      if (gender == null) {
        errors += MISSING_GENDER
      }
    }

    if (phoneNumber != null) {
      errors += when (numberValidator.validate(phoneNumber.number, LANDLINE_OR_MOBILE)) {
        BLANK -> listOf(PHONE_NUMBER_NON_NULL_BUT_BLANK)
        LENGTH_TOO_SHORT -> listOf(PHONE_NUMBER_LENGTH_TOO_SHORT)
        LENGTH_TOO_LONG -> listOf(PHONE_NUMBER_LENGTH_TOO_LONG)
        PhoneNumberValidator.Result.VALID -> listOf()
      }
    }

    if (address == null) {
      errors += EMPTY_ADDRESS_DETAILS
    }

    address?.apply {
      if (colonyOrVillage.isBlank()) {
        errors += COLONY_OR_VILLAGE_EMPTY
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
      val state: String
  ): Parcelable {
    companion object {
      val BLANK = Address("", "", "")

      fun withDistrictAndState(district: String, state: String): Address =
          Address("", district, state)
    }

    fun withColonyOrVillage(colonyOrVillage: String): Address =
        copy(colonyOrVillage = colonyOrVillage)

    fun withDistrict(district: String): Address =
        copy(district = district)

    fun withState(state: String): Address =
        copy(state = state)
  }
}
