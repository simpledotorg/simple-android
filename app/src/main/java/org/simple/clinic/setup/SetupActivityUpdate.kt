package org.simple.clinic.setup

import com.spotify.mobius.Next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.dispatch

class SetupActivityUpdate : Update<SetupActivityModel, SetupActivityEvent, SetupActivityEffect> {

  override fun update(model: SetupActivityModel, event: SetupActivityEvent): Next<SetupActivityModel, SetupActivityEffect> {
    val effect = when (event) {
      is UserDetailsFetched -> if (event.hasUserCompletedOnboarding) GoToMainActivity else ShowOnboardingScreen
    }

    return dispatch(effect)
  }
}
