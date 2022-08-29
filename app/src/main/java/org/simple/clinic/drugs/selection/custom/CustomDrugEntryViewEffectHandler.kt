package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class CustomDrugEntryViewEffectHandler(
    private val uiActions: CustomDrugEntrySheetUiActions
) : ViewEffectsHandler<CustomDrugEntryViewEffect> {

  override fun handle(viewEffect: CustomDrugEntryViewEffect) {
    when (viewEffect) {
      is ShowEditFrequencyDialog -> uiActions.showEditFrequencyDialog(viewEffect.frequency)
      is SetDrugFrequency -> uiActions.setDrugFrequency(viewEffect.frequencyLabel)
      is SetDrugDosage -> uiActions.setDrugDosage(viewEffect.dosage)
      CloseSheetAndGoToEditMedicineScreen -> uiActions.closeSheetAndGoToEditMedicineScreen()
      HideKeyboard -> uiActions.hideKeyboard()
      ShowKeyboard -> uiActions.showKeyboard()
      ClearFocusFromDosageEditText -> uiActions.clearFocusFromDosageEditText()
      is SetCursorPosition -> uiActions.setCursorPosition(viewEffect.position)
    }.exhaustive()
  }
}
