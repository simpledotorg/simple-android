package org.simple.clinic.onboarding

sealed class OnboardingEffect

object CompleteOnboardingEffect : OnboardingEffect()

sealed class OnboardingViewEffect : OnboardingEffect()

object MoveToRegistrationEffect : OnboardingViewEffect()
