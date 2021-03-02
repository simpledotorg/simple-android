package org.simple.clinic.editpatient

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.editpatient.EditPatientValidationError.BothDateOfBirthAndAgeAdsent
import org.simple.clinic.editpatient.EditPatientValidationError.ColonyOrVillageEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthInFuture
import org.simple.clinic.editpatient.EditPatientValidationError.DateOfBirthParseError
import org.simple.clinic.editpatient.EditPatientValidationError.DistrictEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.FullNameEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberEmpty
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooLong
import org.simple.clinic.editpatient.EditPatientValidationError.PhoneNumberLengthTooShort
import org.simple.clinic.editpatient.EditPatientValidationError.StateEmpty
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.registration.phone.PhoneNumberValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputAgeValidator
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator

class EditPatientUpdate(
    private val numberValidator: PhoneNumberValidator,
    private val dobValidator: UserInputDateValidator,
    private val ageValidator: UserInputAgeValidator
) : Update<EditPatientModel, EditPatientEvent, EditPatientEffect> {
  private val errorsForEventType = mapOf(
      PhoneNumberChanged::class to setOf(PhoneNumberEmpty, PhoneNumberLengthTooLong(0), PhoneNumberLengthTooShort(0)),
      NameChanged::class to setOf(FullNameEmpty),
      ColonyOrVillageChanged::class to setOf(ColonyOrVillageEmpty),
      StateChanged::class to setOf(StateEmpty),
      DistrictChanged::class to setOf(DistrictEmpty),
      AgeChanged::class to setOf(BothDateOfBirthAndAgeAdsent),
      DateOfBirthChanged::class to setOf(DateOfBirthParseError, DateOfBirthInFuture)
  )

  override fun update(
      model: EditPatientModel,
      event: EditPatientEvent
  ): Next<EditPatientModel, EditPatientEffect> {
    return when (event) {
      is NameChanged -> onTextFieldChanged(event) { model.updateName(event.name) }
      is GenderChanged -> next(model.updateGender(event.gender))
      is PhoneNumberChanged -> onTextFieldChanged(event) { model.updatePhoneNumber(event.phoneNumber) }
      is ColonyOrVillageChanged -> onTextFieldChanged(event) { model.updateColonyOrVillage(event.colonyOrVillage) }
      is DistrictChanged -> onTextFieldChanged(event) { model.updateDistrict(event.district) }
      is StateChanged -> onTextFieldChanged(event) { model.updateState(event.state) }
      is DateOfBirthFocusChanged -> onDateOfBirthFocusChanged(event)
      is DateOfBirthChanged -> onDateOfBirthChanged(model, event)
      is AgeChanged -> onTextFieldChanged(event) { model.updateAge(event.age) }
      is ZoneChanged -> next(model.updateZone(event.zone))
      is StreetAddressChanged -> next(model.updateStreetAddress(event.streetAddress))
      is BackClicked -> onBackClicked(model)
      is PatientSaved -> next(model.buttonStateChanged(EditPatientState.NOT_SAVING_PATIENT), GoBackEffect)
      is SaveClicked -> onSaveClicked(model)
      is AlternativeIdChanged -> next(model.updateAlternativeId(event.alternativeId))
      is BpPassportsFetched -> dispatch(DisplayBpPassportsEffect(event.bpPasssports))
      is InputFieldsLoaded -> dispatch(SetupUi(event.inputFields) as EditPatientEffect)
      is ColonyOrVillagesFetched -> next(model.updateColonyOrVillagesList(event.colonyOrVillages))
    }
  }

  private fun onTextFieldChanged(
      event: EditPatientEvent,
      modifier: () -> EditPatientModel
  ): Next<EditPatientModel, EditPatientEffect> = next(
      modifier(),
      HideValidationErrorsEffect(errorsForEventType.getValue(event::class))
  )

  private fun onDateOfBirthFocusChanged(
      event: DateOfBirthFocusChanged
  ): Next<EditPatientModel, EditPatientEffect> {
    val showOrHideLabelEffect = if (event.hasFocus) {
      ShowDatePatternInDateOfBirthLabelEffect
    } else {
      HideDatePatternInDateOfBirthLabelEffect
    }
    return dispatch(showOrHideLabelEffect)
  }

  private fun onDateOfBirthChanged(
      model: EditPatientModel,
      event: DateOfBirthChanged
  ): Next<EditPatientModel, EditPatientEffect> {
    val effects = if (event.dateOfBirth.isBlank()) {
      setOf(HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
    } else {
      setOf(ShowDatePatternInDateOfBirthLabelEffect, HideValidationErrorsEffect(errorsForEventType.getValue(event::class)))
    }

    return next(model.updateDateOfBirth(event.dateOfBirth), effects)
  }

  private fun onBackClicked(
      model: EditPatientModel
  ): Next<EditPatientModel, EditPatientEffect> {
    val effect = if (model.savedEntry != model.ongoingEntry) {
      ShowDiscardChangesAlertEffect
    } else {
      GoBackEffect
    }
    return dispatch(effect)
  }

  private fun onSaveClicked(
      model: EditPatientModel
  ): Next<EditPatientModel, EditPatientEffect> {
    val validationErrors = model.ongoingEntry.validate(model.savedPhoneNumber, numberValidator, dobValidator, ageValidator)
    return if (validationErrors.isEmpty()) {
      val (_, ongoingEntry, savedPatient, savedAddress, savedPhoneNumber, savedBangladeshId) = model
      next(model.buttonStateChanged(EditPatientState.SAVING_PATIENT), SavePatientEffect(ongoingEntry, savedPatient, savedAddress, savedPhoneNumber, savedBangladeshId))
    } else {
      dispatch(ShowValidationErrorsEffect(validationErrors))
    }
  }
}
