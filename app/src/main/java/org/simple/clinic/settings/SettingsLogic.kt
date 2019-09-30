package org.simple.clinic.settings

import com.spotify.mobius.First
import com.spotify.mobius.First.first

object SettingsLogic {

  fun init(model: SettingsModel): First<SettingsModel, SettingsEffect> {
    return when {
      model.userDetailsQueried -> first(model)
      else -> first(model, setOf(LoadUserDetailsEffect))
    }
  }
}
