package org.simple.clinic.onboarding

interface OnboardingScreenInjector {
  fun inject(target: OnboardingScreen_Old)
  fun inject(target: OnboardingScreen)
}
