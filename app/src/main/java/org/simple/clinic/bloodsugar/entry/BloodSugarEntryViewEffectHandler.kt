package org.simple.clinic.bloodsugar.entry

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
    }
  }
}
