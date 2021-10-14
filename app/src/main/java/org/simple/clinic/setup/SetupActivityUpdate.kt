package org.simple.clinic.setup

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.setup.runcheck.Allowed
import org.simple.clinic.setup.runcheck.Disallowed
import org.simple.clinic.user.User
import java.net.URI
import java.time.Duration
import java.time.Instant
import java.util.Optional

class SetupActivityUpdate(
    private val databaseMaintenanceInterval: Duration
) : Update<SetupActivityModel, SetupActivityEvent, SetupActivityEffect> {

  constructor(config: SetupActivityConfig) : this(databaseMaintenanceInterval = config.databaseMaintenanceTaskInterval)

  override fun update(
      model: SetupActivityModel,
      event: SetupActivityEvent
  ): Next<SetupActivityModel, SetupActivityEffect> {
    return when (event) {
      is UserDetailsFetched -> {
        val updatedModel = model
            .withLoggedInUser(event.loggedInUser)
            .withSelectedCountry(event.userSelectedCountry)
        val effect = goToNextScreenEffect(event.loggedInUser, event.hasUserCompletedOnboarding, event.userSelectedCountry, event.currentDeployment, event.userSelectedCountryV1)

        next(updatedModel, effect)
      }
      is DatabaseInitialized -> dispatch(FetchDatabaseMaintenanceLastRunAtTime)
      is DatabaseMaintenanceCompleted -> dispatch(FetchUserDetails)
      is DatabaseMaintenanceLastRunAtTimeLoaded -> runDatabaseMaintenanceIfRequired(event, model)
      is AppAllowedToRunCheckCompleted -> initializeDatabase(event)
      is CountryAndDeploymentSaved -> dispatch(DeleteStoredCountryV1)
      is StoredCountryV1Deleted -> dispatch(GoToMainActivity)
    }
  }

  private fun initializeDatabase(
      event: AppAllowedToRunCheckCompleted
  ): Next<SetupActivityModel, SetupActivityEffect> {
    val effect = when (event.allowedToRun) {
      Allowed -> InitializeDatabase
      is Disallowed -> ShowNotAllowedToRunMessage(event.allowedToRun.reason)
    }

    return dispatch(effect)
  }

  private fun runDatabaseMaintenanceIfRequired(
      event: DatabaseMaintenanceLastRunAtTimeLoaded,
      model: SetupActivityModel
  ): Next<SetupActivityModel, SetupActivityEffect> {
    val lastDatabaseMaintenanceRunAt = event.runAt

    val effect = lastDatabaseMaintenanceRunAt
        .map { lastRunAt -> effectForRunningDatabaseMaintenance(lastRunAt, model) }
        .orElse(RunDatabaseMaintenance)

    return dispatch(effect)
  }

  private fun effectForRunningDatabaseMaintenance(
      lastRunAt: Instant,
      model: SetupActivityModel
  ): SetupActivityEffect {
    val shouldRunDatabaseMaintenance = Duration.between(lastRunAt, model.screenOpenedAt) > databaseMaintenanceInterval

    return if (shouldRunDatabaseMaintenance)
      RunDatabaseMaintenance
    else
      FetchUserDetails
  }

  private fun goToNextScreenEffect(
      loggedInUser: Optional<User>,
      hasUserCompletedOnboarding: Boolean,
      selectedCountry: Optional<Country>,
      selectedDeployment: Optional<Deployment>,
      countryV1: Optional<Map<String, String>>
  ): SetupActivityEffect {
    val hasUserLoggedInCompletely = loggedInUser.isPresent && selectedCountry.isPresent && selectedDeployment.isPresent
    val hasUserLoggedInButCountryV1IsPresent = loggedInUser.isPresent && countryV1.isPresent
    val hasUserLoggedInButNoDeploymentIsPresent = loggedInUser.isPresent && selectedCountry.isPresent && !selectedDeployment.isPresent
    val userPresentButCountryNotSelected = loggedInUser.isPresent && !selectedCountry.isPresent

    return when {
      hasUserLoggedInCompletely -> GoToMainActivity
      hasUserLoggedInButCountryV1IsPresent -> {
        val selectedOldCountry = countryV1.get()
        val deployment = Deployment(
            displayName = selectedOldCountry["display_name"]!!,
            endPoint = URI.create(selectedOldCountry["endpoint"]!!)
        )
        val country = Country(
            isoCountryCode = selectedOldCountry["country_code"]!!,
            displayName = selectedOldCountry["display_name"]!!,
            isdCode = selectedOldCountry["isd_code"]!!,
            deployments = listOf(deployment)
        )

        SaveCountryAndDeployment(country, deployment)
      }
      hasUserLoggedInButNoDeploymentIsPresent -> {
        val deployment = selectedCountry.get().deployments.first()

        SaveCountryAndDeployment(selectedCountry.get(), deployment)
      }
      userPresentButCountryNotSelected -> throw IllegalStateException("User is logged in but the selected country is not present.")
      hasUserCompletedOnboarding.not() -> ShowOnboardingScreen
      else -> ShowCountrySelectionScreen
    }
  }
}
