package org.simple.clinic.onboarding

import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch

fun update(
    @Suppress("UNUSED_PARAMETER") model: OnboardingModel,
    event: OnboardingEvent
): Next<OnboardingModel, OnboardingEffect> {
  return when (event) {
    GetStartedClicked -> dispatch(setOf<OnboardingEffect>(CompleteOnboardingEffect))
    OnboardingCompleted -> dispatch(setOf<OnboardingEffect>(MoveToRegistrationEffect))
  }
}
