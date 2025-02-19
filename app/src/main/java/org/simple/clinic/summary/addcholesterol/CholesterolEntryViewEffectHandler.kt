package org.simple.clinic.summary.addcholesterol

import org.simple.clinic.mobius.ViewEffectsHandler

class CholesterolEntryViewEffectHandler(
    private val uiActions: CholesterolEntryUiActions
) : ViewEffectsHandler<CholesterolEntryViewEffect> {

  override fun handle(viewEffect: CholesterolEntryViewEffect) {
    return when (viewEffect) {
      HideCholesterolErrorMessage -> uiActions.hideErrorMessage()
      DismissSheet -> uiActions.dismissSheet()
      ShowReqMaxCholesterolValidationError -> uiActions.showReqMaxCholesterolError()
      ShowReqMinCholesterolValidationError -> uiActions.showReqMinCholesterolError()
    }
  }
}
