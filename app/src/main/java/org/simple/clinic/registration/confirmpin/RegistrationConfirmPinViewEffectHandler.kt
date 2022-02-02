package org.simple.clinic.registration.confirmpin

import org.simple.clinic.mobius.ViewEffectsHandler

class RegistrationConfirmPinViewEffectHandler(
    private val uiActions: RegistrationConfirmPinUiActions
) : ViewEffectsHandler<RegistrationConfirmPinViewEffect> {
  override fun handle(viewEffect: RegistrationConfirmPinViewEffect) {
    when (viewEffect) {
      ClearPin -> uiActions.clearPin()
    }
  }
}
