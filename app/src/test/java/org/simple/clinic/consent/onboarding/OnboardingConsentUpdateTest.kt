package org.simple.clinic.consent.onboarding

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.CompleteOnboardingEffect
import org.simple.clinic.consent.onboarding.OnboardingConsentEffect.MarkDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.AgreeButtonClicked
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.FinishedMarkingDataProtectionConsent

class OnboardingConsentUpdateTest {

  private val updateSpec = UpdateSpec(OnboardingConsentUpdate())

  @Test
  fun `when data protection consent is marked, then complete onboarding`() {
    updateSpec
        .given(OnboardingConsentModel)
        .whenEvent(FinishedMarkingDataProtectionConsent)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CompleteOnboardingEffect)
        ))
  }

  @Test
  fun `when agree button is clicked, then mark data protection consent`() {
    updateSpec
        .given(OnboardingConsentModel)
        .whenEvent(AgreeButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MarkDataProtectionConsent)
        ))
  }
}
