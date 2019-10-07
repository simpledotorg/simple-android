package org.simple.clinic.settings.changelanguage

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.settings.Language

class ChangeLanguageInitTest {

  private val spec = InitSpec<ChangeLanguageModel, ChangeLanguageEffect>(ChangeLanguageLogic::init)
  private val defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES

  @Test
  fun `when the screen is created, the current selected language and the list of supported languages must be loaded`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadCurrentSelectedLanguageEffect, LoadSupportedLanguagesEffect)
        ))
  }

  @Test
  fun `when the screen is restored without supported languages, the list of supported languages must be loaded`() {
    val language = Language(displayName = "English", languageCode = "en_IN")
    val model = defaultModel.withCurrentLanguage(language)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadSupportedLanguagesEffect as ChangeLanguageEffect)
        ))
  }

  @Test
  fun `when the screen is restored without the current selected language, the current selected language must be fetched`() {
    val supportedLanguages = listOf(
        Language(displayName = "English", languageCode = "en_IN"),
        Language(displayName = "हिंदी", languageCode = "hi_IN")
    )
    val model = defaultModel.withSupportedLanguages(supportedLanguages)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadCurrentSelectedLanguageEffect as ChangeLanguageEffect)
        ))
  }

  @Test
  fun `when the screen is restored with the current select language and the supported languages, do nothing`() {
    val language = Language(displayName = "English", languageCode = "en_IN")
    val supportedLanguages = listOf(
        Language(displayName = "English", languageCode = "en_IN"),
        Language(displayName = "हिंदी", languageCode = "hi_IN")
    )

    val model = defaultModel
        .withSupportedLanguages(supportedLanguages)
        .withCurrentLanguage(language)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasNoEffects()
        ))
  }
}
