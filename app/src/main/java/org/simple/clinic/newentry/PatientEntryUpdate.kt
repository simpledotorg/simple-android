package org.simple.clinic.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.newentry.Field.*
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.patient.PatientEntryValidationError.AGE_EXCEEDS_MAX_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.AGE_EXCEEDS_MIN_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.BOTH_DATEOFBIRTH_AND_AGE_ABSENT
import org.simple.clinic.patient.PatientEntryValidationError.COLONY_OR_VILLAGE_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.DATE_OF_BIRTH_IN_FUTURE
import org.simple.clinic.patient.PatientEntryValidationError.DISTRICT_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.DOB_EXCEEDS_MAX_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.DOB_EXCEEDS_MIN_LIMIT
import org.simple.clinic.patient.PatientEntryValidationError.FULL_NAME_EMPTY
import org.simple.clinic.patient.PatientEntryValidationError.INVALID_DATE_OF_BIRTH
import org.simple.clinic.patient.PatientEntryValidationError.MISSING_GENDER
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_LONG
import org.simple.clinic.patient.PatientEntryValidationError.PHONE_NUMBER_LENGTH_TOO_SHORT
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.Optional
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator

typealias PatientEntryNext = Next<PatientEntryModel, PatientEntryEffect>

class PatientEntryUpdate(
    private val phoneNumberValidator: PhoneNumberValidator,
    private val dobValidator: UserInputDateValidator,
    private val ageValidator: UserInputAgeValidator
) : Update<PatientEntryModel, PatientEntryEvent, PatientEntryEffect> {
  override fun update(model: PatientEntryModel, event: PatientEntryEvent): PatientEntryNext {
    return when (event) {
      is FullNameChanged -> onFieldChanged(model.fullNameChanged(event.fullName), FullName)
      is PhoneNumberChanged -> onFieldChanged(model.phoneNumberChanged(event.phoneNumber), PhoneNumber)
      is AgeChanged -> onFieldChanged(model.ageChanged(event.age), Age)
      is DateOfBirthChanged -> onFieldChanged(model.dateOfBirthChanged(event.dateOfBirth), DateOfBirth)
      is ColonyOrVillageChanged -> onFieldChanged(model.colonyOrVillageChanged(event.colonyOrVillage), ColonyOrVillage)
      is DistrictChanged -> onFieldChanged(model.districtChanged(event.district), District)
      is StateChanged -> onFieldChanged(model.stateChanged(event.state), State)
      is BangladeshNationalIdChanged -> onFieldChanged(model.bangladeshNationalIdChanged(event.bangladeshNationalId), BangladeshNationalId)
      is GenderChanged -> onGenderChanged(model, event.gender)
      is StreetAddressChanged -> onFieldChanged(model.streetAddressChanged(event.streetAddress), StreetAddress)
      is ZoneChanged -> onFieldChanged(model.zoneChanged(event.zone), Zone)
      is OngoingEntryFetched -> onOngoingEntryFetched(model, event.patientEntry)
      is DateOfBirthFocusChanged -> onDateOfBirthFocusChanged(model, event.hasFocus)
      is ReminderConsentChanged -> next(model.reminderConsentChanged(event.reminderConsent))
      is SaveClicked -> onSaveClicked(model, model.patientEntry)
      is PatientEntrySaved -> dispatch(OpenMedicalHistoryEntryScreen)
    }
  }

  private fun onFieldChanged(updatedModel: PatientEntryModel, changedField: Field): PatientEntryNext =
      next(updatedModel, HideValidationError(changedField))

  private fun onOngoingEntryFetched(
      model: PatientEntryModel,
      patientEntry: OngoingNewPatientEntry
  ): PatientEntryNext =
      next(model.patientEntryFetched(patientEntry), PrefillFields(patientEntry))

  private fun onGenderChanged(model: PatientEntryModel, gender: Optional<Gender>): PatientEntryNext {
    val updatedModel = model.genderChanged(gender)
    return if (gender.isNotEmpty() && model.isSelectingGenderForTheFirstTime) {
      next(updatedModel, HideValidationError(Gender), ScrollFormOnGenderSelection)
    } else {
      next(updatedModel)
    }
  }

  private fun onDateOfBirthFocusChanged(model: PatientEntryModel, hasFocus: Boolean): PatientEntryNext {
    val hasDateOfBirth = model.patientEntry.personalDetails?.dateOfBirth?.isNotBlank() == true
    return dispatch(ShowDatePatternInDateOfBirthLabel(hasFocus || hasDateOfBirth))
  }

  private fun onSaveClicked(
      model: PatientEntryModel,
      patientEntry: OngoingNewPatientEntry
  ): PatientEntryNext {
    val validationErrors = patientEntry.validationErrors(dobValidator, phoneNumberValidator, ageValidator)
    return if (validationErrors.isEmpty()) {
      dispatch(SavePatient(patientEntry))
    } else {
      return when (validationErrors) {
        listOf(FULL_NAME_EMPTY) -> next(model.validationFailed(FULL_NAME_EMPTY))
        listOf(PHONE_NUMBER_LENGTH_TOO_SHORT) -> next(model.validationFailed(PHONE_NUMBER_LENGTH_TOO_SHORT))
        listOf(PHONE_NUMBER_LENGTH_TOO_LONG) -> next(model.validationFailed(PHONE_NUMBER_LENGTH_TOO_LONG))
        listOf(BOTH_DATEOFBIRTH_AND_AGE_ABSENT) -> next(model.validationFailed(BOTH_DATEOFBIRTH_AND_AGE_ABSENT))
        listOf(INVALID_DATE_OF_BIRTH) -> next(model.validationFailed(INVALID_DATE_OF_BIRTH))
        listOf(DATE_OF_BIRTH_IN_FUTURE) -> next(model.validationFailed(DATE_OF_BIRTH_IN_FUTURE))
        listOf(MISSING_GENDER) -> next(model.validationFailed(MISSING_GENDER))
        listOf(COLONY_OR_VILLAGE_EMPTY) -> next(model.validationFailed(COLONY_OR_VILLAGE_EMPTY))
        listOf(DISTRICT_EMPTY) -> next(model.validationFailed(DISTRICT_EMPTY))
        listOf(AGE_EXCEEDS_MAX_LIMIT) -> next(model.validationFailed(AGE_EXCEEDS_MAX_LIMIT))
        listOf(AGE_EXCEEDS_MIN_LIMIT) -> next(model.validationFailed(AGE_EXCEEDS_MIN_LIMIT))
        listOf(DOB_EXCEEDS_MAX_LIMIT) -> next(model.validationFailed(DOB_EXCEEDS_MAX_LIMIT))
        listOf(DOB_EXCEEDS_MIN_LIMIT) -> next(model.validationFailed(DOB_EXCEEDS_MIN_LIMIT))
        else -> dispatch(ShowValidationErrors(validationErrors))
      }
    }
  }
}
