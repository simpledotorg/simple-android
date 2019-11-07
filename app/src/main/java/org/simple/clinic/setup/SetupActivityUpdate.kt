package org.simple.clinic.setup

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional

class SetupActivityUpdate : Update<SetupActivityModel, SetupActivityEvent, SetupActivityEffect> {

  override fun update(model: SetupActivityModel, event: SetupActivityEvent): Next<SetupActivityModel, SetupActivityEffect> {
    val effect = when (event) {
      is UserDetailsFetched -> initialScreenEffect(event.loggedInUser, event.hasUserCompletedOnboarding)
      is DatabaseInitialized -> FetchUserDetails
      else -> null
    }

    return if (effect == null) noChange() else dispatch(effect)
  }

  private fun initialScreenEffect(
      loggedInUser: Optional<User>,
      hasUserCompletedOnboarding: Boolean
  ): SetupActivityEffect {
    return when {
      loggedInUser is Just -> GoToMainActivity
      hasUserCompletedOnboarding.not() -> ShowOnboardingScreen
      else -> ShowCountrySelectionScreen
    }
  }
}
