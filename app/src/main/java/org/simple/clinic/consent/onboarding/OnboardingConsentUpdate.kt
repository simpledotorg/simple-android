package org.simple.clinic.consent.onboarding

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.CompleteOnboardingEffect
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.MarkDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.AgreeButtonClicked
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.FinishedMarkingDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.OnboardingCompleted
import org.simple.clinic.consent.onboarding.OnboardingConsentViewEffect.MoveToRegistrationActivity
import org.simple.clinic.mobius.dispatch

class OnboardingConsentUpdate : Update<OnboardingConsentModel, OnboardingConsentEvent, OnboardingConsentEffect> {

  override fun update(model: OnboardingConsentModel, event: OnboardingConsentEvent): Next<OnboardingConsentModel, OnboardingConsentEffect> {
    return when (event) {
      FinishedMarkingDataProtectionConsent -> dispatch(CompleteOnboardingEffect)
      AgreeButtonClicked -> dispatch(MarkDataProtectionConsent)
      OnboardingCompleted -> dispatch(MoveToRegistrationActivity)
    }
  }
}
