package org.simple.clinic.settings

import org.simple.clinic.mobius.ViewRenderer

class SettingsUiRenderer(private val ui: SettingsUi): ViewRenderer<SettingsModel> {
  override fun render(model: SettingsModel) {
    if(model.userDetailsQueried) {
      requireNotNull(model.name)
      requireNotNull(model.phoneNumber)
      ui.displayUserDetails(model.name, model.phoneNumber)
    }
  }
}
