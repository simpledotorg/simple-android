package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.exhaustive

class PatientEntryViewEffectHandler(
    private val uiActions: PatientEntryUiActions
) : ViewEffectsHandler<PatientEntryViewEffect> {

  private val showDatePatternInLabelValueChangedCallback = ValueChangedCallback<Boolean>()

  override fun handle(viewEffect: PatientEntryViewEffect) {
    when (viewEffect) {
      is PrefillFields -> uiActions.prefillFields(viewEffect.patientEntry)
      ScrollFormOnGenderSelection -> uiActions.scrollFormOnGenderSelection()
      is ShowDatePatternInDateOfBirthLabel -> showDatePatternInLabelValueChangedCallback.pass(viewEffect.show,
          uiActions::setShowDatePatternInDateOfBirthLabel)
      OpenMedicalHistoryEntryScreen -> uiActions.openMedicalHistoryEntryScreen()
      is SetupUi -> uiActions.setupUi(viewEffect.inputFields)
      is HideValidationError -> hideValidationError(viewEffect.field)
      is ShowValidationErrors -> showValidationErrors(viewEffect.errors)
    }.exhaustive()
  }

  private fun hideValidationError(field: Field) {
    when (field) {
      Field.FullName -> uiActions.showEmptyFullNameError(false)
      Field.PhoneNumber -> hidePhoneLengthErrors()
      Field.Age, Field.DateOfBirth -> hideDateOfBirthErrors()
      Field.Gender -> uiActions.showMissingGenderError(false)
      Field.ColonyOrVillage -> uiActions.showEmptyColonyOrVillageError(false)
      Field.District -> uiActions.showEmptyDistrictError(false)
      Field.State -> uiActions.showEmptyStateError(false)
    }
  }

  private fun hidePhoneLengthErrors() {
    with(uiActions) {
      showLengthTooShortPhoneNumberError(false, 0)
    }
  }

  private fun hideDateOfBirthErrors() {
    with(uiActions) {
      showEmptyDateOfBirthAndAgeError(false)
      showInvalidDateOfBirthError(false)
      showDateOfBirthIsInFutureError(false)
      showAgeExceedsMaxLimitError(false)
      showDOBExceedsMaxLimitError(false)
      showAgeExceedsMinLimitError(false)
      showDOBExceedsMinLimitError(false)
    }
  }

  private fun showValidationErrors(errors: List<PatientEntryValidationError>) {
    errors
        .onEach { Analytics.reportInputValidationError(it.analyticsName) }
        .forEach {
          when (it) {
            PatientEntryValidationError.FullNameEmpty -> uiActions.showEmptyFullNameError(true)
            is PatientEntryValidationError.PhoneNumberLengthTooShort -> uiActions.showLengthTooShortPhoneNumberError(true, it.limit)
            PatientEntryValidationError.BothDateOfBirthAndAgeAbsent -> uiActions.showEmptyDateOfBirthAndAgeError(true)
            PatientEntryValidationError.InvalidDateOfBirth -> uiActions.showInvalidDateOfBirthError(true)
            PatientEntryValidationError.DateOfBirthInFuture -> uiActions.showDateOfBirthIsInFutureError(true)
            PatientEntryValidationError.MissingGender -> uiActions.showMissingGenderError(true)
            PatientEntryValidationError.ColonyOrVillageEmpty -> uiActions.showEmptyColonyOrVillageError(true)
            PatientEntryValidationError.DistrictEmpty -> uiActions.showEmptyDistrictError(true)
            PatientEntryValidationError.StateEmpty -> uiActions.showEmptyStateError(true)
            PatientEntryValidationError.AgeExceedsMaxLimit -> uiActions.showAgeExceedsMaxLimitError(true)
            PatientEntryValidationError.DobExceedsMaxLimit -> uiActions.showDOBExceedsMaxLimitError(true)
            PatientEntryValidationError.AgeExceedsMinLimit -> uiActions.showAgeExceedsMinLimitError(true)
            PatientEntryValidationError.DobExceedsMinLimit -> uiActions.showDOBExceedsMinLimitError(true)
            PatientEntryValidationError.EmptyAddressDetails,
            PatientEntryValidationError.PhoneNumberNonNullButBlank,
            PatientEntryValidationError.BothDateOfBirthAndAgePresent,
            PatientEntryValidationError.PersonalDetailsEmpty -> {
              throw AssertionError("Should never receive this error: $it")
            }
          }
        }

    if (errors.isNotEmpty()) {
      uiActions.scrollToFirstFieldWithError()
    }
  }
}
