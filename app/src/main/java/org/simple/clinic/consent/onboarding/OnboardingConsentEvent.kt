package org.simple.clinic.consent.onboarding

sealed interface OnboardingConsentEvent {
  data object FinishedMarkingDataProtectionConsent : OnboardingConsentEvent
}
