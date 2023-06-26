package org.simple.clinic.settings

import org.simple.clinic.mobius.ViewEffectsHandler
import org.simple.clinic.util.exhaustive

class SettingsViewEffectHandler(
    private val uiActions: UiActions
) : ViewEffectsHandler<SettingsViewEffect> {

  override fun handle(viewEffect: SettingsViewEffect) {
    when (viewEffect) {
      OpenLanguageSelectionScreenEffect -> uiActions.openLanguageSelectionScreen()
      ShowConfirmLogoutDialog -> uiActions.showConfirmLogoutDialog()
    }.exhaustive()
  }
}
