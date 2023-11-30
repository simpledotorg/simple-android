package org.simple.clinic.consent.onboarding

import org.simple.clinic.mobius.ViewEffectsHandler

class OnboardingConsentViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<OnboardingConsentViewEffect> {

  override fun handle(viewEffect: OnboardingConsentViewEffect) {
    when (viewEffect) {
      is OnboardingConsentViewEffect.OpenCountrySelectionScreen -> {
        uiActions.openCountrySelectionScreen()
      }
    }
  }
}
