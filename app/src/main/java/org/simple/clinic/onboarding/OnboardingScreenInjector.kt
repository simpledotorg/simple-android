package org.simple.clinic.onboarding

interface OnboardingScreenInjector {
  companion object {
    const val INJECTOR_KEY = "OnboardingScreenInjector"
  }

  fun inject(target: OnboardingScreen)
}
