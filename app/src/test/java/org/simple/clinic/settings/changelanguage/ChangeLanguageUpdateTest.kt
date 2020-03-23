package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.settings.ProvidedLanguage

class ChangeLanguageUpdateTest {

  private val defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES

  private val englishIndia = ProvidedLanguage(displayName = "English", languageCode = "en_IN")
  private val hindiIndia = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")

  private val spec = UpdateSpec<ChangeLanguageModel, ChangeLanguageEvent, ChangeLanguageEffect>(ChangeLanguageUpdate())

  @Test
  fun `when the current language is loaded, the ui must be updated`() {
    spec
        .given(defaultModel)
        .whenEvent(CurrentLanguageLoadedEvent(englishIndia))
        .then(assertThatNext(
            hasModel(defaultModel.withCurrentLanguage(englishIndia)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the supported languages are loaded, the ui must be updated`() {
    val supportedLanguages = listOf(englishIndia, hindiIndia)

    spec
        .given(defaultModel)
        .whenEvent(SupportedLanguagesLoadedEvent(supportedLanguages))
        .then(assertThatNext(
            hasModel(defaultModel.withSupportedLanguages(supportedLanguages)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the user selects a language, the ui must be updated`() {
    val model = defaultModel
        .withCurrentLanguage(englishIndia)
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))

    spec
        .given(model)
        .whenEvent(SelectLanguageEvent(hindiIndia))
        .then(assertThatNext(
            hasModel(model.withUserSelectedLanguage(hindiIndia)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the current language is changed, then restart the activity`() {
    val model = defaultModel
        .withCurrentLanguage(englishIndia)
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))

    spec
        .given(model)
        .whenEvent(CurrentLanguageChangedEvent)
        .then(assertThatNext(
            hasModel(model.restarted()),
            hasEffects(RestartActivity, TriggerSync)
        ))
  }

  @Test
  fun `when the user clicks save, the current language must be set to the user selected language`() {
    val model = defaultModel
        .withCurrentLanguage(englishIndia)
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))
        .withUserSelectedLanguage(hindiIndia)

    spec
        .given(model)
        .whenEvent(SaveCurrentLanguageEvent)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(UpdateCurrentLanguageEffect(hindiIndia) as ChangeLanguageEffect)
        ))
  }
}
