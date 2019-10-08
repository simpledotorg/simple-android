package org.simple.clinic.onboarding

import com.spotify.mobius.Next
import org.simple.clinic.mobius.justEffect

fun onboardingUpdate(
    @Suppress("UNUSED_PARAMETER") model: OnboardingModel,
    event: OnboardingEvent
): Next<OnboardingModel, OnboardingEffect> {
  return when (event) {
    GetStartedClicked -> justEffect(CompleteOnboardingEffect)
    OnboardingCompleted -> justEffect(MoveToRegistrationEffect)
  }
}
