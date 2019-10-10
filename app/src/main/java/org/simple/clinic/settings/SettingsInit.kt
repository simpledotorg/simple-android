package org.simple.clinic.settings

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class SettingsInit : Init<SettingsModel, SettingsEffect> {

  override fun init(model: SettingsModel): First<SettingsModel, SettingsEffect> {
    return when {
      model.userDetailsQueried -> first(model)
      else -> first(model, setOf(LoadUserDetailsEffect))
    }
  }
}
