package org.simple.clinic.consent.onboarding

sealed interface OnboardingConsentEffect {

  data object MarkDataProtectionConsent : OnboardingConsentEffect

  data object CompleteOnboardingEffect : OnboardingConsentEffect
}

sealed interface OnboardingConsentViewEffect : OnboardingConsentEffect {

  data object MoveToRegistrationActivity : OnboardingConsentViewEffect
}
