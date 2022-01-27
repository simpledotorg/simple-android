package org.simple.clinic.settings

import org.simple.clinic.mobius.ViewEffectsHandler

class SettingsViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<SettingsViewEffect> {

  override fun handle(viewEffect: SettingsViewEffect) {
    // does absolute nothing for now
  }
}
