package org.simple.clinic.newentry

import org.simple.clinic.mobius.ViewEffectsHandler
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
      else -> throw IllegalArgumentException("Cannot hide error for field: ${field.name}")
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
}
