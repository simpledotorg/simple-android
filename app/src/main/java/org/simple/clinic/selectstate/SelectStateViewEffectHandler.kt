package org.simple.clinic.selectstate

import org.simple.clinic.mobius.ViewEffectsHandler

class SelectStateViewEffectHandler(
    private val uiActions: SelectStateUiActions
) : ViewEffectsHandler<SelectStateViewEffect> {

  override fun handle(viewEffect: SelectStateViewEffect) {
    when (viewEffect) {
      GoToRegistrationScreen -> uiActions.goToRegistrationScreen()
      ReplaceCurrentScreenWithRegistrationScreen -> uiActions.replaceCurrentScreenToRegistrationScreen()
    }
  }
}
