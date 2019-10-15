package org.simple.clinic.settings

import org.simple.clinic.mobius.ViewRenderer

class SettingsUiRenderer(private val ui: SettingsUi) : ViewRenderer<SettingsModel> {

  override fun render(model: SettingsModel) {
    renderUserDetails(model)
    renderCurrentLanguage(model)
  }

  private fun renderUserDetails(model: SettingsModel) {
    if (model.userDetailsQueried) {
      requireNotNull(model.name)
      requireNotNull(model.phoneNumber)
      ui.displayUserDetails(model.name, model.phoneNumber)
    }
  }

  private fun renderCurrentLanguage(model: SettingsModel) {
    if (model.currentLanguageQueried) {
      val currentLanguage = model.currentLanguage
      requireNotNull(currentLanguage)

      ui.setChangeLanguageButtonVisible()
      if (currentLanguage is ProvidedLanguage) {
        ui.displayCurrentLanguage(currentLanguage.displayName)
      }
    }
  }
}
