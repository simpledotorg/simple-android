package org.simple.clinic.registration.confirmpin

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class RegistrationConfirmPinViewEffectHandler(
    private val uiActions: RegistrationConfirmPinUiActions
) : ViewEffectsHandler<RegistrationConfirmPinViewEffect> {
  override fun handle(viewEffect: RegistrationConfirmPinViewEffect) {
    when (viewEffect) {
      ClearPin -> uiActions.clearPin()
      is OpenFacilitySelectionScreen -> uiActions.openFacilitySelectionScreen(viewEffect.entry)
      is GoBackToPinEntry -> uiActions.goBackToPinScreen(viewEffect.entry)
    }.exhaustive()
  }
}
