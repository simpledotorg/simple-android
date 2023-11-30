package org.simple.clinic.onboarding

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class OnboardingViewEffectHandler(
    private val uiActions: OnboardingUi
) : ViewEffectsHandler<OnboardingViewEffect> {

  override fun handle(viewEffect: OnboardingViewEffect) {
    when (viewEffect) {
      OpenOnboardingConsentScreen -> uiActions.openOnboardingConsentScreen()
    }.exhaustive()
  }
}
