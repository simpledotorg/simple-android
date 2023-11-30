package org.simple.clinic.consent.onboarding

sealed interface OnboardingConsentEffect {

  data object MarkDataProtectionConsent : OnboardingConsentEffect
}
