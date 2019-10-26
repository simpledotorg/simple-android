package org.simple.clinic.setup

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class SetupActivityUpdateTest {

  private val updateSpec = UpdateSpec(SetupActivityUpdate())
  private val defaultModel = SetupActivityModel

  @Test
  fun `if the user has completed onboarding, the main activity must be opened`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(UserDetailsFetched(hasUserCompletedOnboarding = true))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToMainActivity as SetupActivityEffect)
        ))
  }

  @Test
  fun `if the user has not completed onboarding, the onboarding screen must be shown`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(UserDetailsFetched(hasUserCompletedOnboarding = false))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowOnboardingScreen as SetupActivityEffect)
        ))
  }

  @Test
  fun `when the database completes initialization, the user details must be fetched`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DatabaseInitialized)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(FetchUserDetails as SetupActivityEffect)
            )
        )
  }
}
