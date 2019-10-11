package org.simple.clinic.onboarding

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.justEffect

class OnboardingUpdate : Update<OnboardingModel, OnboardingEvent, OnboardingEffect> {
  override fun update(model: OnboardingModel, event: OnboardingEvent): Next<OnboardingModel, OnboardingEffect> {
    return when (event) {
      GetStartedClicked -> justEffect(CompleteOnboardingEffect)
      OnboardingCompleted -> justEffect(MoveToRegistrationEffect)
    }
  }
}

