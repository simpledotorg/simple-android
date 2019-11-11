package org.simple.clinic.setup

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.appconfig.Country
import org.simple.clinic.patient.PatientMocker
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.toOptional
import java.util.UUID

class SetupActivityUpdateTest {

  private val updateSpec = UpdateSpec(SetupActivityUpdate())
  private val defaultModel = SetupActivityModel.SETTING_UP

  @Test
  fun `if the user has not logged in, the country selection screen must be shown`() {
    val expectedModel = defaultModel
        .withLoggedInUser(None)
        .withSelectedCountry(None)

    updateSpec
        .given(defaultModel)
        .whenEvent(onboardedUserWithoutLoggingIn())
        .then(assertThatNext(
            hasModel(expectedModel),
            hasEffects(ShowCountrySelectionScreen as SetupActivityEffect)
        ))
  }

  @Test
  fun `if the user has not completed onboarding, the onboarding screen must be shown`() {
    val expectedModel = defaultModel
        .withLoggedInUser(None)
        .withSelectedCountry(None)

    updateSpec
        .given(defaultModel)
        .whenEvent(completelyNewUser())
        .then(assertThatNext(
            hasModel(expectedModel),
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
  fun `if the user has logged in and a country is selected, go to home screen`() {
    // given
    val user = PatientMocker.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))
    val country = PatientMocker.country()

    //then
    val expectedModel = defaultModel
        .withLoggedInUser(user.toOptional())
        .withSelectedCountry(country.toOptional())

    updateSpec
        .given(defaultModel)
        .whenEvent(loggedInUser(user, country))
        .then(assertThatNext(
            hasModel(expectedModel),
            hasEffects(GoToMainActivity as SetupActivityEffect)
        ))
  }

  @Test
  fun `if a logged in user has updated the app without selecting a country, set the fallback country as the selected country`() {
    // given
    val user = PatientMocker.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))

    // then
    val expectedModel = defaultModel
        .withLoggedInUser(user.toOptional())
        .withSelectedCountry(None)

    updateSpec
        .given(defaultModel)
        .whenEvent(previouslyLoggedInUser(user))
        .then(assertThatNext(
            hasModel(expectedModel),
            hasEffects(SetFallbackCountryAsCurrentCountry as SetupActivityEffect)
        ))
  }

  @Test
  fun `when the fallback country is set as the selected country, go to home screen`() {
    // given
    val user = PatientMocker.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))
    val model = defaultModel
        .withLoggedInUser(user.toOptional())
        .withSelectedCountry(None)

    // then
    updateSpec
        .given(model)
        .whenEvent(FallbackCountrySetAsSelected)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToMainActivity as SetupActivityEffect)
        ))
  }

  private fun previouslyLoggedInUser(user: User): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = Just(user), userSelectedCountry = None)
  }

  private fun onboardedUserWithoutLoggingIn(): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = None, userSelectedCountry = None)
  }

  private fun completelyNewUser(): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = false, loggedInUser = None, userSelectedCountry = None)
  }

  private fun loggedInUser(user: User, country: Country): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = Just(user), userSelectedCountry = Just(country))
  }
}
