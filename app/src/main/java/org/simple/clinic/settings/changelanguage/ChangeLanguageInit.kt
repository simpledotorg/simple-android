package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Init

class ChangeLanguageInit : Init<ChangeLanguageModel, ChangeLanguageEffect> {

  override fun init(model: ChangeLanguageModel): First<ChangeLanguageModel, ChangeLanguageEffect> {
    val supportedLanguagesNotLoaded = model.supportedLanguages.isEmpty()
    val currentLanguageNotLoaded = model.currentLanguage == null

    val effects = when {
      currentLanguageNotLoaded && supportedLanguagesNotLoaded -> setOf(LoadCurrentLanguageEffect, LoadSupportedLanguagesEffect)
      currentLanguageNotLoaded -> setOf(LoadCurrentLanguageEffect)
      supportedLanguagesNotLoaded -> setOf(LoadSupportedLanguagesEffect)
      else -> emptySet()
    }

    return if (effects.isNotEmpty()) first(model, effects) else first(model)
  }
}
