package org.simple.clinic.settings.changelanguage

import org.simple.clinic.mobius.ViewRenderer
import org.simple.clinic.settings.Language

class ChangeLanguageUiRenderer(val ui: ChangeLanguageUi) : ViewRenderer<ChangeLanguageModel> {

  override fun render(model: ChangeLanguageModel) {
    if (model.haveLanguagesBeenFetched) {
      ui.displayLanguages(model.supportedLanguages, model.userSelectedLanguage)

      toggleDoneButtonEnabledState(model.userSelectedLanguage)
    }
  }

  private fun toggleDoneButtonEnabledState(userSelectedLanguage: Language?) {
    if (userSelectedLanguage == null) {
      ui.setDoneButtonDisabled()
    } else {
      ui.setDoneButtonEnabled()
    }
  }
}
