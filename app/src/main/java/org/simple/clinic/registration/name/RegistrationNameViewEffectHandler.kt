package org.simple.clinic.registration.name

import org.simple.clinic.mobius.ViewEffectsHandler

class RegistrationNameViewEffectHandler(
    private val uiActions: RegistrationNameUiActions
) : ViewEffectsHandler<RegistrationNameViewEffect> {
  override fun handle(viewEffect: RegistrationNameViewEffect) {
    when (viewEffect) {
      is PrefillFields -> uiActions.preFillUserDetails(viewEffect.entry)
      is ProceedToPinEntry -> uiActions.openRegistrationPinEntryScreen(viewEffect.entry)
    }
  }
}
