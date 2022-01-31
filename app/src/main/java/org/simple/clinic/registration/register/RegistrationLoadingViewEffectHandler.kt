package org.simple.clinic.registration.register

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class RegistrationLoadingViewEffectHandler(
    private val uiActions: RegistrationLoadingUiActions
) : ViewEffectsHandler<RegistrationLoadingViewEffect> {

  override fun handle(viewEffect: RegistrationLoadingViewEffect) {
    when (viewEffect) {
      GoToHomeScreen -> uiActions.openHomeScreen()
    }.exhaustive()
  }
}
