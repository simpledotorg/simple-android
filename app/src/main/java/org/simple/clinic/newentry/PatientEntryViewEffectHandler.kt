package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.patient.PatientEntryValidationError
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.util.ValueChangedCallback
import org.simple.clinic.util.exhaustive

class PatientEntryViewEffectHandler(
    private val uiActions: PatientEntryUiActions,
    private val validationActions: PatientEntryValidationActions
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
      Field.FullName -> validationActions.showEmptyFullNameError(false)
      Field.PhoneNumber -> hidePhoneLengthErrors()
      Field.Age, Field.DateOfBirth -> hideDateOfBirthErrors()
      Field.Gender -> validationActions.showMissingGenderError(false)
      Field.ColonyOrVillage -> validationActions.showEmptyColonyOrVillageError(false)
      Field.District -> validationActions.showEmptyDistrictError(false)
      Field.State -> validationActions.showEmptyStateError(false)
    }
  }

  private fun hidePhoneLengthErrors() {
    with(validationActions) {
      showLengthTooShortPhoneNumberError(false, 0)
    }
  }

  private fun hideDateOfBirthErrors() {
    with(validationActions) {
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
            PatientEntryValidationError.FullNameEmpty -> validationActions.showEmptyFullNameError(true)
            is PatientEntryValidationError.PhoneNumberLengthTooShort -> validationActions.showLengthTooShortPhoneNumberError(true, it.limit)
            PatientEntryValidationError.BothDateOfBirthAndAgeAbsent -> validationActions.showEmptyDateOfBirthAndAgeError(true)
            PatientEntryValidationError.InvalidDateOfBirth -> validationActions.showInvalidDateOfBirthError(true)
            PatientEntryValidationError.DateOfBirthInFuture -> validationActions.showDateOfBirthIsInFutureError(true)
            PatientEntryValidationError.MissingGender -> validationActions.showMissingGenderError(true)
            PatientEntryValidationError.ColonyOrVillageEmpty -> validationActions.showEmptyColonyOrVillageError(true)
            PatientEntryValidationError.DistrictEmpty -> validationActions.showEmptyDistrictError(true)
            PatientEntryValidationError.StateEmpty -> validationActions.showEmptyStateError(true)
            PatientEntryValidationError.AgeExceedsMaxLimit -> validationActions.showAgeExceedsMaxLimitError(true)
            PatientEntryValidationError.DobExceedsMaxLimit -> validationActions.showDOBExceedsMaxLimitError(true)
            PatientEntryValidationError.AgeExceedsMinLimit -> validationActions.showAgeExceedsMinLimitError(true)
            PatientEntryValidationError.DobExceedsMinLimit -> validationActions.showDOBExceedsMinLimitError(true)
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
