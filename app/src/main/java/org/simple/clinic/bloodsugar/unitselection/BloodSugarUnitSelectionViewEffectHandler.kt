package org.simple.clinic.bloodsugar.unitselection

import org.simple.clinic.mobius.ViewEffectsHandler

class BloodSugarUnitSelectionViewEffectHandler(
    private val uiActions: BloodSugarUnitSelectionUiActions
) : ViewEffectsHandler<BloodSugarUnitSelectionViewEffect> {

  override fun handle(viewEffect: BloodSugarUnitSelectionViewEffect) {
    when (viewEffect) {
      CloseDialog -> uiActions.closeDialog()
      is PreFillBloodSugarUnitSelected -> uiActions.prefillBloodSugarUnitSelection(viewEffect.bloodSugarUnitPreference)
    }
  }
}
