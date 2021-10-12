package org.simple.clinic.setup

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.setup.runcheck.Allowed
import org.simple.clinic.setup.runcheck.Disallowed
import org.simple.clinic.setup.runcheck.Disallowed.Reason.Rooted
import org.simple.clinic.user.User
import org.simple.clinic.util.TestUtcClock
import java.time.Duration
import java.time.Instant
import java.util.Optional
import java.util.UUID

class SetupActivityUpdateTest {

  private val databaseMaintenanceInterval = Duration.ofDays(1)
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
  fun `when the database completes initialization, the last database maintenance run time must be fetched`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DatabaseInitialized)
        .then(
            assertThatNext(
                hasNoModel(),
                hasEffects(FetchDatabaseMaintenanceLastRunAtTime)
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
  fun `when the database maintenance is completed, fetch the user details`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DatabaseMaintenanceCompleted)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(FetchUserDetails)
        ))
  }

  @Test
  fun `when the database maintenance task has never been run, run the database maintenance task`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(DatabaseMaintenanceLastRunAtTimeLoaded(Optional.empty()))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RunDatabaseMaintenance)
        ))
  }

  @Test
  fun `when the database maintenance task has not been run in a while, run the database maintenance task`() {
    val lastDatabaseRunTime = currentTime.minus(databaseMaintenanceInterval.plusMillis(1))

    updateSpec
        .given(defaultModel)
        .whenEvent(DatabaseMaintenanceLastRunAtTimeLoaded(Optional.of(lastDatabaseRunTime)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RunDatabaseMaintenance)
        ))
  }

  @Test
  fun `when the database maintenance task has been run recently, fetch the user details`() {
    val lastDatabaseRunTime = currentTime.minus(databaseMaintenanceInterval)

    updateSpec
        .given(defaultModel)
        .whenEvent(DatabaseMaintenanceLastRunAtTimeLoaded(Optional.of(lastDatabaseRunTime)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(FetchUserDetails)
        ))
  }

  @Test
  fun `when the app is allowed to run, initialize the database`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(AppAllowedToRunCheckCompleted(Allowed))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(InitializeDatabase)
        ))
  }

  @Test
  fun `when the app is not allowed to run, show the error message`() {
    val disallowedReason = Rooted

    updateSpec
        .given(defaultModel)
        .whenEvent(AppAllowedToRunCheckCompleted(Disallowed(disallowedReason)))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowNotAllowedToRunMessage(disallowedReason))
        ))
  }

  private fun previouslyLoggedInUserFetched(user: User): UserDetailsFetched {
    return UserDetailsFetched(
        hasUserCompletedOnboarding = true,
        loggedInUser = Optional.of(user),
        userSelectedCountry = Optional.empty(),
        userSelectedCountryV1 = Optional.empty(),
        currentDeployment = Optional.empty()
    )
  }

  private fun onboardedUserWithoutLoggingInFetched(): UserDetailsFetched {
    return UserDetailsFetched(
        hasUserCompletedOnboarding = true,
        loggedInUser = Optional.empty(),
        userSelectedCountry = Optional.empty(),
        userSelectedCountryV1 = Optional.empty(),
        currentDeployment = Optional.empty()
    )
  }

  private fun completelyNewUserFetched(): UserDetailsFetched {
    return UserDetailsFetched(
        hasUserCompletedOnboarding = false,
        loggedInUser = Optional.empty(),
        userSelectedCountry = Optional.empty(),
        userSelectedCountryV1 = Optional.empty(),
        currentDeployment = Optional.empty()
    )
  }

  private fun loggedInUserFetched(user: User, country: Country): UserDetailsFetched {
    return UserDetailsFetched(
        hasUserCompletedOnboarding = true,
        loggedInUser = Optional.of(user),
        userSelectedCountry = Optional.of(country),
        userSelectedCountryV1 = Optional.empty(),
        currentDeployment = Optional.empty()
    )
  }
}

private fun SetupActivityModel.previouslyLoggedInUser(user: User): SetupActivityModel {
  return this
      .withLoggedInUser(Optional.of(user))
      .withSelectedCountry(Optional.empty())
}

private fun SetupActivityModel.completelyNewUser(): SetupActivityModel {
  return this
      .withLoggedInUser(Optional.empty())
      .withSelectedCountry(Optional.empty())
}

private fun SetupActivityModel.loggedInUser(user: User, country: Country): SetupActivityModel {
  return this
      .withLoggedInUser(Optional.of(user))
      .withSelectedCountry(Optional.of(country))
}
