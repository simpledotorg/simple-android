package org.simple.clinic.onboarding

import org.simple.clinic.mobius.ViewEffectsHandler

class OnboardingViewEffectHandler(
    private val uiActions: OnboardingUi
) : ViewEffectsHandler<OnboardingViewEffect> {

  override fun handle(viewEffect: OnboardingViewEffect) {
    // Keep moving, nothing to look here
  }
}
