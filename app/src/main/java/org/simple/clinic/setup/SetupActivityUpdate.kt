package org.simple.clinic.setup

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.appconfig.Country
import org.simple.clinic.appconfig.Deployment
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.setup.runcheck.Allowed
import org.simple.clinic.setup.runcheck.Disallowed
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
      is UserDetailsFetched -> processUserDetails(event, model)
      is DatabaseInitialized -> dispatch(FetchDatabaseMaintenanceLastRunAtTime)
      is DatabaseMaintenanceCompleted -> dispatch(FetchUserDetails)
      is DatabaseMaintenanceLastRunAtTimeLoaded -> runDatabaseMaintenanceIfRequired(event, model)
      is AppAllowedToRunCheckCompleted -> initializeDatabase(event)
      is CountryAndDeploymentSaved -> dispatch(DeleteStoredCountryV1)
      is StoredCountryV1Deleted -> dispatch(GoToMainActivity)
    }
  }

  private fun processUserDetails(
      event: UserDetailsFetched,
      model: SetupActivityModel
  ): Next<SetupActivityModel, SetupActivityEffect> {
    val loggedInUser = event.loggedInUser
    val selectedCountry = event.userSelectedCountry
    val selectedDeployment = event.currentDeployment
    val countryV1 = event.userSelectedCountryV1

    val hasUserLoggedInCompletely = loggedInUser.isPresent && selectedCountry.isPresent && selectedDeployment.isPresent
    val hasUserLoggedInButCountryV1IsPresent = loggedInUser.isPresent && countryV1.isPresent
    val hasUserLoggedInButNoDeploymentIsPresent = loggedInUser.isPresent && selectedCountry.isPresent && !selectedDeployment.isPresent
    val userPresentButCountryNotSelected = loggedInUser.isPresent && !selectedCountry.isPresent
    val shouldConstructCountryAndDeployment = hasUserLoggedInButCountryV1IsPresent || hasUserLoggedInButNoDeploymentIsPresent

    val effect = if (shouldConstructCountryAndDeployment) {
      saveCountryAndDeploymentEffect(
          countryV1IsPresent = hasUserLoggedInButCountryV1IsPresent,
          countryV1 = countryV1,
          deploymentIsAbsent = hasUserLoggedInButNoDeploymentIsPresent,
          selectedCountry = selectedCountry
      )
    } else {
      goToNextScreenEffect(
          hasUserCompletedOnboarding = event.hasUserCompletedOnboarding,
          hasUserLoggedInCompletely = hasUserLoggedInCompletely,
          countryNotSelected = userPresentButCountryNotSelected
      )
    }

    return dispatch(effect)
  }

  private fun saveCountryAndDeploymentEffect(
      countryV1IsPresent: Boolean,
      countryV1: Optional<Map<String, String>>,
      deploymentIsAbsent: Boolean,
      selectedCountry: Optional<Country>
  ): SetupActivityEffect {
    return when {
      countryV1IsPresent -> {
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
      deploymentIsAbsent -> {
        val deployment = selectedCountry.get().deployments.first()

        SaveCountryAndDeployment(selectedCountry.get(), deployment)
      }
      else -> throw RuntimeException("This should never happen!")
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
      hasUserCompletedOnboarding: Boolean,
      hasUserLoggedInCompletely: Boolean,
      countryNotSelected: Boolean
  ): SetupActivityEffect {
    return when {
      hasUserLoggedInCompletely -> GoToMainActivity
      countryNotSelected -> throw IllegalStateException("User is logged in but the selected country is not present.")
      hasUserCompletedOnboarding.not() -> ShowOnboardingScreen
      else -> ShowCountrySelectionScreen
    }
  }
}
