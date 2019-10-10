package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.Next
import com.spotify.mobius.Next.next
import com.spotify.mobius.Update
import org.simple.clinic.mobius.justEffect

class ChangeLanguageUpdate : Update<ChangeLanguageModel, ChangeLanguageEvent, ChangeLanguageEffect> {

  override fun update(model: ChangeLanguageModel, event: ChangeLanguageEvent): Next<ChangeLanguageModel, ChangeLanguageEffect> {
    return when (event) {
      is CurrentLanguageLoadedEvent -> next(model.withCurrentLanguage(event.language))
      is SupportedLanguagesLoadedEvent -> next(model.withSupportedLanguages(event.languages))
      is SelectLanguageEvent -> next(model.withUserSelectedLanguage(event.newLanguage))
      is CurrentLanguageChangedEvent -> justEffect(GoBack)
      is SaveCurrentLanguageEvent -> justEffect(UpdateCurrentLanguageEffect(model.userSelectedLanguage!!))
    }
  }
}
