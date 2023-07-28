package org.simple.clinic.settings

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class SettingsInit : Init<SettingsModel, SettingsEffect> {

  override fun init(model: SettingsModel): First<SettingsModel, SettingsEffect> {
    val effects = mutableSetOf(LoadCurrentLanguageEffect, CheckAppUpdateAvailable)

    if (!model.userDetailsQueried) {
      effects.add(LoadUserDetailsEffect)
    }

    if (!model.appVersionQueried) {
      effects.add(LoadAppVersionEffect)
    }

    return first(model, effects)
  }
}
