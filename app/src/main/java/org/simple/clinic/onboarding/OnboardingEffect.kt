package org.simple.clinic.onboarding

sealed class OnboardingEffect

sealed class OnboardingViewEffect : OnboardingEffect()

data object OpenOnboardingConsentScreen : OnboardingViewEffect()
