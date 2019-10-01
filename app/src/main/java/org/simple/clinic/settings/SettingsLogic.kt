package org.simple.clinic.settings

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.next

object SettingsLogic {

  fun init(model: SettingsModel): First<SettingsModel, SettingsEffect> {
    return when {
      model.userDetailsQueried -> first(model)
      else -> first(model, setOf(LoadUserDetailsEffect))
    }
  }

  fun update(model: SettingsModel, event: SettingsEvent): Next<SettingsModel, SettingsEffect> {
    return when (event) {
      is UserDetailsLoaded -> next(model.userDetailsFetched(name = event.name, phoneNumber = event.phoneNumber))
    }
  }
}
