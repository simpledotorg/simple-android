package org.simple.clinic.consent.onboarding

import org.simple.clinic.widgets.UiEvent

sealed interface OnboardingConsentEvent : UiEvent {

  data object FinishedMarkingDataProtectionConsent : OnboardingConsentEvent

  data object AgreeButtonClicked : OnboardingConsentEvent {
    override val analyticsName: String = "Onboarding Consent Screen:Agree Button Clicked"
  }

  data object OnboardingCompleted : OnboardingConsentEvent
}
