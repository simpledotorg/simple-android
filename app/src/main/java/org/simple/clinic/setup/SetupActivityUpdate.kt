package org.simple.clinic.setup

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.appconfig.Country
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.setup.runcheck.Allowed
import org.simple.clinic.setup.runcheck.Disallowed
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.isEmpty
import org.simple.clinic.util.isNotEmpty
import java.time.Duration
import java.time.Instant

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
        val effect = goToNextScreenEffect(event.loggedInUser, event.hasUserCompletedOnboarding, event.userSelectedCountry)

        next(updatedModel, effect)
      }
      is DatabaseInitialized -> dispatch(FetchDatabaseMaintenanceLastRunAtTime)
      is FallbackCountrySetAsSelected -> dispatch(GoToMainActivity)
      is DatabaseMaintenanceCompleted -> dispatch(FetchUserDetails)
      is DatabaseMaintenanceLastRunAtTimeLoaded -> runDatabaseMaintenanceIfRequired(event, model)
      is AppAllowedToRunCheckCompleted -> initializeDatabase(event)
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
      selectedCountry: Optional<Country>
  ): SetupActivityEffect {
    val hasUserLoggedInCompletely = loggedInUser.isNotEmpty() && selectedCountry.isNotEmpty()
    val userPresentButCountryNotSelected = loggedInUser.isNotEmpty() && selectedCountry.isEmpty()

    return when {
      hasUserLoggedInCompletely -> GoToMainActivity
      userPresentButCountryNotSelected -> SetFallbackCountryAsCurrentCountry
      hasUserCompletedOnboarding.not() -> ShowOnboardingScreen
      else -> ShowCountrySelectionScreen
    }
  }
}
