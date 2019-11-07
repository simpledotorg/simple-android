package org.simple.clinic.setup

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.util.None
import org.simple.clinic.util.toOptional
import java.util.UUID

class SetupActivityUpdateTest {

  private val updateSpec = UpdateSpec(SetupActivityUpdate())
  private val defaultModel = SetupActivityModel

  @Test
  fun `if the user has not logged in, the country selection screen must be shown`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = None))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowCountrySelectionScreen as SetupActivityEffect)
        ))
  }

  @Test
  fun `if the user has not completed onboarding, the onboarding screen must be shown`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(UserDetailsFetched(hasUserCompletedOnboarding = false, loggedInUser = None))
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

  @Test
  fun `if the user has logged in, go to home screen`() {
    val user = PatientMocker.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))
    updateSpec
        .given(defaultModel)
        .whenEvent(UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = user.toOptional()))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToMainActivity as SetupActivityEffect)
        ))
  }
}
