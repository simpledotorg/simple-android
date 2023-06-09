package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.mobius.ViewEffectsHandler

class BloodSugarEntryViewEffectHandler(
    private val uiActions: BloodSugarEntryUiActions
) : ViewEffectsHandler<BloodSugarEntryViewEffect> {

  override fun handle(viewEffect: BloodSugarEntryViewEffect) {
    when (viewEffect) {
      is SetBloodSugarReading -> uiActions.setBloodSugarReading(viewEffect.bloodSugarReading)
      is HideBloodSugarErrorMessage -> uiActions.hideBloodSugarErrorMessage()
      is HideDateErrorMessage -> uiActions.hideDateErrorMessage()
      is Dismiss -> uiActions.dismiss()
      is ShowDateEntryScreen -> uiActions.showDateEntryScreen()
      is SetBloodSugarSavedResultAndFinish -> uiActions.setBloodSugarSavedResultAndFinish()
      is ShowConfirmRemoveBloodSugarDialog -> uiActions.showConfirmRemoveBloodSugarDialog(viewEffect.bloodSugarMeasurementUuid)
      is ShowBloodSugarUnitSelectionDialog -> uiActions.showBloodSugarUnitSelectionDialog(viewEffect.bloodSugarUnitPreference)
      is ShowBloodSugarValidationError -> showBloodSugarValidationError(viewEffect.result, viewEffect.unitPreference)
    }
  }

  private fun showBloodSugarValidationError(
      result: ValidationResult,
      unitPreference: BloodSugarUnitPreference
  ) {
    when (result) {
      ValidationResult.ErrorBloodSugarEmpty -> uiActions.showBloodSugarEmptyError()
      is ValidationResult.ErrorBloodSugarTooHigh -> uiActions.showBloodSugarHighError(result.measurementType, unitPreference)
      is ValidationResult.ErrorBloodSugarTooLow -> uiActions.showBloodSugarLowError(result.measurementType, unitPreference)
      is ValidationResult.Valid -> {
        /* no-op */
      }
    }
  }
}
