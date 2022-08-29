package org.simple.clinic.registration.pin

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class RegistrationPinViewEffectHandler(
    private val uiActions: RegistrationPinUiActions
) : ViewEffectsHandler<RegistrationPinViewEffect> {

  override fun handle(viewEffect: RegistrationPinViewEffect) {
    when (viewEffect) {
      is ProceedToConfirmPin -> uiActions.openRegistrationConfirmPinScreen(viewEffect.entry)
    }.exhaustive()
  }
}
