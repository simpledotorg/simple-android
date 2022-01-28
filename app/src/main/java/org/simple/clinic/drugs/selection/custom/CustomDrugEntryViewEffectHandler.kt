package org.simple.clinic.drugs.selection.custom

import org.simple.clinic.mobius.ViewEffectsHandler

class CustomDrugEntryViewEffectHandler(
    private val uiActions: CustomDrugEntrySheetUiActions
) : ViewEffectsHandler<CustomDrugEntryViewEffect> {

  override fun handle(viewEffect: CustomDrugEntryViewEffect) {
    // nothing to look at here, yet
  }
}
