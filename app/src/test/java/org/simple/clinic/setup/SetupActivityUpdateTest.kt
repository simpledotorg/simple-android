package org.simple.clinic.setup

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.setup.runcheck.Allowed
import org.simple.clinic.setup.runcheck.Disallowed
import org.simple.clinic.setup.runcheck.Disallowed.Reason.Rooted
import org.simple.clinic.user.User
import org.simple.clinic.util.TestUtcClock
import java.net.URI
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
    updateSpec
        .given(defaultModel)
        .whenEvent(onboardedUserWithoutLoggingInFetched())
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowCountrySelectionScreen as SetupActivityEffect)
        ))
  }

  @Test
  fun `if the user has not completed onboarding, the onboarding screen must be shown`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(completelyNewUserFetched())
        .then(assertThatNext(
            hasNoModel(),
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
  fun `if the user has logged in completely, go to home screen`() {
    // given
    val user = TestData.loggedInUser(uuid = UUID.fromString("d7349b2e-bcc8-47d4-be29-1775b88e8460"))
    val country = TestData.country()

    //then
    updateSpec
        .given(defaultModel)
        .whenEvent(loggedInUserFetched(user, country, country.deployments.first()))
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

  @Test
  fun `when country and deployment is saved, then delete stored country v1 preference`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(CountryAndDeploymentSaved)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(DeleteStoredCountryV1)
        ))
  }

  @Test
  fun `when stored country v1 is deleted, then go to main activity`() {
    updateSpec
        .given(defaultModel)
        .whenEvent(StoredCountryV1Deleted)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(GoToMainActivity)
        ))
  }

  @Test
  fun `when user is logged in and the v1 country is present, migrate the country to the new format`() {
    val user = TestData.loggedInUser(uuid = UUID.fromString("85233c9e-edda-417e-8f58-8f1413ac84a1"))
    val countryV1 = mapOf(
        "country_code" to "IN",
        "endpoint" to "https://api.simple.org/api/v1",
        "display_name" to "India",
        "isd_code" to "91"
    )

    val event = UserDetailsFetched(
        hasUserCompletedOnboarding = true,
        loggedInUser = Optional.of(user),
        userSelectedCountry = Optional.empty(),
        userSelectedCountryV1 = Optional.of(countryV1),
        currentDeployment = Optional.empty()
    )

    val expectedDeploymentToSave = Deployment(
        displayName = "India",
        endPoint = URI.create("https://api.simple.org/api/v1")
    )
    val expectedCountryToSave = Country(
        isoCountryCode = "IN",
        displayName = "India",
        isdCode = "91",
        deployments = listOf(expectedDeploymentToSave)
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(event)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveCountryAndDeployment(expectedCountryToSave, expectedDeploymentToSave))
        ))
  }

  @Test
  fun `when user is logged in with v2 country and there is no deployment present, add a deployment from the v2 country`() {
    val user = TestData.loggedInUser(uuid = UUID.fromString("85233c9e-edda-417e-8f58-8f1413ac84a1"))

    val deployment = Deployment(
        displayName = "India",
        endPoint = URI.create("https://api.simple.org/api/v1")
    )
    val country = Country(
        isoCountryCode = "IN",
        displayName = "India",
        isdCode = "91",
        deployments = listOf(deployment)
    )

    val event = UserDetailsFetched(
        hasUserCompletedOnboarding = true,
        loggedInUser = Optional.of(user),
        userSelectedCountry = Optional.of(country),
        userSelectedCountryV1 = Optional.empty(),
        currentDeployment = Optional.empty()
    )

    updateSpec
        .given(defaultModel)
        .whenEvent(event)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(SaveCountryAndDeployment(country, deployment))
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

  private fun loggedInUserFetched(
      user: User,
      country: Country,
      deployment: Deployment
  ): UserDetailsFetched {
    return UserDetailsFetched(
        hasUserCompletedOnboarding = true,
        loggedInUser = Optional.of(user),
        userSelectedCountry = Optional.of(country),
        userSelectedCountryV1 = Optional.empty(),
        currentDeployment = Optional.of(deployment)
    )
  }
}
