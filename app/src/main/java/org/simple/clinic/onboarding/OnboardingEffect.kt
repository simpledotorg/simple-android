package org.simple.clinic.onboarding

sealed class OnboardingEffect

object CompleteOnboardingEffect : OnboardingEffect()

object MoveToRegistrationEffect : OnboardingEffect()
