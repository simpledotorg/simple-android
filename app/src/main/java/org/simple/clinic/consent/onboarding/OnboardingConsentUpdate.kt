package org.simple.clinic.consent.onboarding

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.MarkDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.AgreeButtonClicked
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.FinishedMarkingDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentViewEffect.OpenCountrySelectionScreen
import org.simple.clinic.mobius.dispatch

class OnboardingConsentUpdate : Update<OnboardingConsentModel, OnboardingConsentEvent, OnboardingConsentEffect> {

  override fun update(model: OnboardingConsentModel, event: OnboardingConsentEvent): Next<OnboardingConsentModel, OnboardingConsentEffect> {
    return when (event) {
      FinishedMarkingDataProtectionConsent -> dispatch(OpenCountrySelectionScreen)
      AgreeButtonClicked -> dispatch(MarkDataProtectionConsent)
    }
  }
}