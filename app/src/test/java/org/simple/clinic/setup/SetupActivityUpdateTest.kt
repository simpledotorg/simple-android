package org.simple.clinic.setup

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.util.UUID

class SetupActivityUpdateTest {

  private val databaseMaintenanceInterval = Duration.ZERO
  private val updateSpec = UpdateSpec(SetupActivityUpdate(databaseMaintenanceInterval))
  private val currentTime = Instant.parse("2018-01-01T00:00:00Z")
  private val clock = TestUtcClock(currentTime)
  private val defaultModel = SetupActivityModel.create(clock)

  @Test
  fun `if the user has not logged in, the country selection screen must be shown`() {
    val expectedModel = defaultModel.completelyNewUser()

    updateSpec
        .given(defaultModel)
        .whenEvent(onboardedUserWithoutLoggingInFetched())
        .then(assertThatNext(
            hasModel(expectedModel),
            hasEffects(ShowCountrySelectionScreen as SetupActivityEffect)
        ))
  }

  @Test
  fun `if the user has not completed onboarding, the onboarding screen must be shown`() {
    val expectedModel = defaultModel.completelyNewUser()

    updateSpec
        .given(defaultModel)
        .whenEvent(completelyNewUserFetched())
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
    val user = TestData.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))
    val country = TestData.country()

    //then
    val expectedModel = defaultModel.loggedInUser(user, country)

    updateSpec
        .given(defaultModel)
        .whenEvent(loggedInUserFetched(user, country))
        .then(assertThatNext(
            hasModel(expectedModel),
            hasEffects(GoToMainActivity as SetupActivityEffect)
        ))
  }

  @Test
  fun `if a logged in user has updated the app without selecting a country, set the fallback country as the selected country`() {
    // given
    val user = TestData.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))

    // then
    val expectedModel = defaultModel.previouslyLoggedInUser(user)

    updateSpec
        .given(defaultModel)
        .whenEvent(previouslyLoggedInUserFetched(user))
        .then(assertThatNext(
            hasModel(expectedModel),
            hasEffects(SetFallbackCountryAsCurrentCountry as SetupActivityEffect)
        ))
  }

  @Test
  fun `when the fallback country is set as the selected country, go to home screen`() {
    // given
    val user = TestData.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))
    val model = defaultModel.previouslyLoggedInUser(user)

    // then
    updateSpec
        .given(model)
        .whenEvent(FallbackCountrySetAsSelected)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToMainActivity as SetupActivityEffect)
        ))
  }

  @Test
  fun `when the database maintenance is completed, fetch the user details`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DatabaseMaintenanceCompleted)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(FetchUserDetails)
        ))
  }

  private fun previouslyLoggedInUserFetched(user: User): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = Just(user), userSelectedCountry = None())
  }

  private fun onboardedUserWithoutLoggingInFetched(): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = None(), userSelectedCountry = None())
  }

  private fun completelyNewUserFetched(): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = false, loggedInUser = None(), userSelectedCountry = None())
  }

  private fun loggedInUserFetched(user: User, country: Country): UserDetailsFetched {
    return UserDetailsFetched(hasUserCompletedOnboarding = true, loggedInUser = Just(user), userSelectedCountry = Just(country))
  }
}

private fun SetupActivityModel.previouslyLoggedInUser(user: User): SetupActivityModel {
  return this
      .withLoggedInUser(Just(user))
      .withSelectedCountry(None())
}

private fun SetupActivityModel.completelyNewUser(): SetupActivityModel {
  return this
      .withLoggedInUser(None())
      .withSelectedCountry(None())
}

private fun SetupActivityModel.loggedInUser(user: User, country: Country): SetupActivityModel {
  return this
      .withLoggedInUser(Just(user))
      .withSelectedCountry(Just(country))
}
