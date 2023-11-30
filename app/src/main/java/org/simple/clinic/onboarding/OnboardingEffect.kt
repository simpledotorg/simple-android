package org.simple.clinic.onboarding

sealed class OnboardingEffect

object CompleteOnboardingEffect : OnboardingEffect()

sealed class OnboardingViewEffect : OnboardingEffect()

data object OpenOnboardingConsentScreen : OnboardingViewEffect()
