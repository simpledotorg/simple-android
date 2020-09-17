package org.simple.clinic.setup

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.appconfig.Country
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.user.User
import org.simple.clinic.util.Optional
import org.simple.clinic.util.isEmpty
import org.simple.clinic.util.isNotEmpty
import java.time.Duration

class SetupActivityUpdate(
    private val databaseMaintenanceInterval: Duration
) : Update<SetupActivityModel, SetupActivityEvent, SetupActivityEffect> {

  constructor(config: SetupActivityConfig): this(databaseMaintenanceInterval = config.databaseMaintenanceTaskInterval)

  override fun update(model: SetupActivityModel, event: SetupActivityEvent): Next<SetupActivityModel, SetupActivityEffect> {
    return when (event) {
      is UserDetailsFetched -> {
        val updatedModel = model
            .withLoggedInUser(event.loggedInUser)
            .withSelectedCountry(event.userSelectedCountry)
        val effect = goToNextScreenEffect(event.loggedInUser, event.hasUserCompletedOnboarding, event.userSelectedCountry)

        next(updatedModel, effect)
      }
      is DatabaseInitialized -> dispatch(FetchUserDetails)
      is FallbackCountrySetAsSelected -> dispatch(GoToMainActivity)
      else -> noChange()
    }
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
