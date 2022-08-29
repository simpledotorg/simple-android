package org.simple.clinic.onboarding

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class OnboardingUpdateTest {

  private val updateSpec = UpdateSpec(OnboardingUpdate())
  private val defaultModel = OnboardingModel

  @Test
  fun `when get started button is clicked, then set onboarding as completed`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(GetStartedClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(CompleteOnboardingEffect)
        ))
  }

  @Test
  fun `when onboarding is completed, then move to registration screen`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(OnboardingCompleted)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(MoveToRegistrationEffect)
        ))
  }
}
