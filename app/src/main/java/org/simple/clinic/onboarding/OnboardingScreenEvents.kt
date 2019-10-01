package org.simple.clinic.onboarding

import org.simple.clinic.widgets.UiEvent

sealed class OnboardingEvent : UiEvent

class OnboardingGetStartedClicked : OnboardingEvent() {
  override val analyticsName = "Onboarding:Get Started Clicked"
}
