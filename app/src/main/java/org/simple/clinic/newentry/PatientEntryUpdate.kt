package org.simple.clinic.newentry

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.newentry.Field.*
import org.simple.clinic.patient.Gender
import org.simple.clinic.patient.OngoingNewPatientEntry
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.util.Optional
import org.simple.clinic.util.isNotEmpty
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
      is AlternativeIdChanged -> onFieldChanged(model.alternativeIdChanged(event.identifier), BangladeshNationalId)
      is GenderChanged -> onGenderChanged(model, event.gender)
      is StreetAddressChanged -> onFieldChanged(model.streetAddressChanged(event.streetAddress), StreetAddress)
      is ZoneChanged -> onFieldChanged(model.zoneChanged(event.zone), Zone)
      is OngoingEntryFetched -> onOngoingEntryFetched(model, event.patientEntry)
      is DateOfBirthFocusChanged -> onDateOfBirthFocusChanged(model, event.hasFocus)
      is ReminderConsentChanged -> next(model.reminderConsentChanged(event.reminderConsent))
      is SaveClicked -> onSaveClicked(model)
      is PatientEntrySaved -> next(model.buttonStateChanged(ButtonState.SAVED), OpenMedicalHistoryEntryScreen)
      is InputFieldsLoaded -> dispatch(SetupUi(event.inputFields))
      is ColonyOrVillagesFetched -> next(model.colonyOrVillageListUpdated(event.colonyOrVillages))
    }
  }

  private fun onFieldChanged(
      updatedModel: PatientEntryModel,
      changedField: Field
  ): PatientEntryNext =
      next(updatedModel, HideValidationError(changedField))

  private fun onOngoingEntryFetched(
      model: PatientEntryModel,
      patientEntry: OngoingNewPatientEntry
  ): PatientEntryNext =
      next(model.patientEntryFetched(patientEntry), PrefillFields(patientEntry))

  private fun onGenderChanged(
      model: PatientEntryModel,
      gender: Optional<Gender>
  ): PatientEntryNext {
    val updatedModel = model.genderChanged(gender)
    return if (gender.isNotEmpty() && model.isSelectingGenderForTheFirstTime) {
      next(updatedModel, HideValidationError(Field.Gender), ScrollFormOnGenderSelection)
    } else {
      next(updatedModel)
    }
  }

  private fun onDateOfBirthFocusChanged(
      model: PatientEntryModel,
      hasFocus: Boolean
  ): PatientEntryNext {
    val hasDateOfBirth = model.patientEntry.personalDetails?.dateOfBirth?.isNotBlank() == true
    return dispatch(ShowDatePatternInDateOfBirthLabel(hasFocus || hasDateOfBirth))
  }

  private fun onSaveClicked(
      model: PatientEntryModel
  ): PatientEntryNext {
    val validationErrors = model.patientEntry.validationErrors(dobValidator, phoneNumberValidator, ageValidator)
    return if (validationErrors.isEmpty()) {
      next(model.buttonStateChanged(ButtonState.SAVING), SavePatient(model.patientEntry))
    } else {
      dispatch(ShowValidationErrors(validationErrors))
    }
  }
}
