package org.simple.clinic.selectcountry

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class SelectCountryViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<SelectCountryViewEffect> {

  override fun handle(viewEffect: SelectCountryViewEffect) {
    when (viewEffect) {
      GoToStateSelectionScreen -> uiActions.goToStateSelectionScreen()
    }.exhaustive()
  }
}
