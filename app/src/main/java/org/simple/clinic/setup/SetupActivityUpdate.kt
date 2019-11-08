package org.simple.clinic.setup

import com.spotify.mobius.Next
import com.spotify.mobius.Next.noChange
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch
import org.simple.clinic.mobius.next
import org.simple.clinic.user.User
import org.simple.clinic.util.Just
import org.simple.clinic.util.None
import org.simple.clinic.util.Optional

class SetupActivityUpdate : Update<SetupActivityModel, SetupActivityEvent, SetupActivityEffect> {

  override fun update(model: SetupActivityModel, event: SetupActivityEvent): Next<SetupActivityModel, SetupActivityEffect> {
    return when (event) {
      is UserDetailsFetched -> {
        val updatedModel = model
            .withLoggedInUser(event.loggedInUser)
            .withSelectedCountry(None)
        val effect = initialScreenEffect(event.loggedInUser, event.hasUserCompletedOnboarding)

        next(updatedModel, effect)
      }
      is DatabaseInitialized -> dispatch(FetchUserDetails)
      else -> noChange()
    }
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
