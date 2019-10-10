package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next

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

    return if (effects.isNotEmpty()) first(model, effects) else first(model)
  }

  fun update(model: ChangeLanguageModel, event: ChangeLanguageEvent): Next<ChangeLanguageModel, ChangeLanguageEffect> {
    return when (event) {
      is CurrentSelectedLanguageLoadedEvent -> next(model.withCurrentLanguage(event.language))
      is SupportedLanguagesLoadedEvent -> next(model.withSupportedLanguages(event.languages))
      is SelectLanguageEvent -> dispatch(setOf(UpdateSelectedLanguageEffect(event.newLanguage)))
      is SelectedLanguageChangedEvent -> next(model.withCurrentLanguage(event.selectedLanguage))
    }
  }
}
