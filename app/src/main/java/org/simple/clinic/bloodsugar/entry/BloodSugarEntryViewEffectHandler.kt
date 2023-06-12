package org.simple.clinic.bloodsugar.entry

import org.simple.clinic.bloodsugar.BloodSugarUnitPreference
import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive
import org.simple.clinic.widgets.ageanddateofbirth.UserInputDateValidator
import java.time.LocalDate

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
      is ShowDateValidationError -> showDateValidationError(viewEffect.result)
      is ShowBloodSugarEntryScreen -> showBloodSugarEntryScreen(viewEffect.date)
      is PrefillDates -> prefillDates(viewEffect.date)
    }
  }

  private fun prefillDates(date: LocalDate) {
    with(uiActions) {
      showBloodSugarDate(date)
      setDateOnInputFields(date)
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

  private fun showDateValidationError(result: UserInputDateValidator.Result) {
    when (result) {
      UserInputDateValidator.Result.Invalid.InvalidPattern -> uiActions.showInvalidDateError()
      UserInputDateValidator.Result.Invalid.DateIsInFuture -> uiActions.showDateIsInFutureError()
      is UserInputDateValidator.Result.Valid -> throw IllegalStateException("Date validation error cannot be $result")
    }.exhaustive()
  }

  private fun showBloodSugarEntryScreen(date: LocalDate) {
    with(uiActions) {
      showBloodSugarEntryScreen()
      showBloodSugarDate(date)
    }
  }
}
