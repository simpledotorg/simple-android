package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.First
import com.spotify.mobius.First.first

object ChangeLanguageLogic {

  fun init(model: ChangeLanguageModel): First<ChangeLanguageModel, ChangeLanguageEffect> {
    val supportedLanguagesNotLoaded = model.supportedLanguages.isEmpty()
    val currentSelectedLanguageNotLoaded = model.currentLanguage == null

    val effects = when {
      currentSelectedLanguageNotLoaded && supportedLanguagesNotLoaded -> setOf(LoadCurrentSelectedLanguageEffect, LoadSupportedLanguagesEffect)
      currentSelectedLanguageNotLoaded -> setOf(LoadCurrentSelectedLanguageEffect)
      supportedLanguagesNotLoaded -> setOf(LoadSupportedLanguagesEffect)
      else -> emptySet()
    }

    return if(effects.isNotEmpty()) first(model, effects) else first(model)
  }
}
