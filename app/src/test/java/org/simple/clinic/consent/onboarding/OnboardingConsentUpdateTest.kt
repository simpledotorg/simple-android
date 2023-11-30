package org.simple.clinic.consent.onboarding

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.consent.onboarding.OnboardingConsentEvent.FinishedMarkingDataProtectionConsent
import org.simple.clinic.consent.onboarding.OnboardingConsentViewEffect.OpenCountrySelectionScreen

class OnboardingConsentUpdateTest {

  @Test
  fun `when data protection consent is marked, then open country selection screen`() {
    UpdateSpec(OnboardingConsentUpdate())
        .given(OnboardingConsentModel)
        .whenEvent(FinishedMarkingDataProtectionConsent)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenCountrySelectionScreen)
        ))
  }
}
