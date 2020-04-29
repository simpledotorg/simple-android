package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.settings.ProvidedLanguage

class ChangeLanguageInitTest {

  private val spec = InitSpec<ChangeLanguageModel, ChangeLanguageEffect>(ChangeLanguageInit())
  private val defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES

  private val english = ProvidedLanguage(displayName = "English", languageCode = "en-IN")
  private val hindi = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi-IN")

  @Test
  fun `when the screen is created, the current language and the list of supported languages must be loaded`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadCurrentLanguageEffect, LoadSupportedLanguagesEffect)
        ))
  }

  @Test
  fun `when the screen is restored without supported languages, the list of supported languages must be loaded`() {
    val model = defaultModel.withCurrentLanguage(english)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadSupportedLanguagesEffect as ChangeLanguageEffect)
        ))
  }

  @Test
  fun `when the screen is restored without the current language, the current language must be fetched`() {
    val model = defaultModel.withSupportedLanguages(listOf(english, hindi))

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadCurrentLanguageEffect as ChangeLanguageEffect)
        ))
  }

  @Test
  fun `when the screen is restored with the current language and the supported languages, do nothing`() {
    val model = defaultModel
        .withSupportedLanguages(listOf(english, hindi))
        .withCurrentLanguage(english)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the screen is restored after a manual restart, the screen must be closed`() {
    val model = defaultModel
        .withSupportedLanguages(listOf(english, hindi))
        .withCurrentLanguage(english)
        .withUserSelectedLanguage(english)
        .restarted()

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(GoBack as ChangeLanguageEffect)
        ))
  }
}
