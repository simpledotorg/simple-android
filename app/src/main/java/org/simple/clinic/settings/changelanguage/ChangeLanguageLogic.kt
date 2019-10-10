package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.First
import com.spotify.mobius.First.first
import com.spotify.mobius.Next
import com.spotify.mobius.Next.dispatch
import com.spotify.mobius.Next.next

object ChangeLanguageLogic {

  fun init(model: ChangeLanguageModel): First<ChangeLanguageModel, ChangeLanguageEffect> {
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

  fun update(model: ChangeLanguageModel, event: ChangeLanguageEvent): Next<ChangeLanguageModel, ChangeLanguageEffect> {
    return when (event) {
      is CurrentLanguageLoadedEvent -> next(model.withCurrentLanguage(event.language))
      is SupportedLanguagesLoadedEvent -> next(model.withSupportedLanguages(event.languages))
      is SelectLanguageEvent -> next(model.withUserSelectedLanguage(event.newLanguage))
      is CurrentLanguageChangedEvent -> dispatch(setOf(GoBack))
      is SaveCurrentLanguageEvent -> dispatch(setOf(UpdateCurrentLanguageEffect(model.userSelectedLanguage!!)))
    }
  }
}
