package org.simple.clinic.settings.changelanguage

import org.simple.clinic.settings.Language

interface ChangeLanguageUi {

  fun displayLanguages(supportedLanguages: List<Language>, selectedLanguage: Language?)

  fun setDoneButtonDisabled()

  fun setDoneButtonEnabled()
}
