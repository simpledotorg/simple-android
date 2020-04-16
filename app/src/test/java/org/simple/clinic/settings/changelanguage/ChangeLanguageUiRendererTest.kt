package org.simple.clinic.settings.changelanguage

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import org.junit.Test
import org.simple.clinic.settings.ProvidedLanguage
import org.simple.clinic.settings.SystemDefaultLanguage

class ChangeLanguageUiRendererTest {

  private val ui = mock<ChangeLanguageUi>()
  private val defaultModel = ChangeLanguageModel.FETCHING_LANGUAGES

  private val englishIndia = ProvidedLanguage(displayName = "English", languageCode = "en_IN")
  private val hindiIndia = ProvidedLanguage(displayName = "हिंदी", languageCode = "hi_IN")
  private val supportedLanguages = listOf(englishIndia, hindiIndia)

  private val renderer = ChangeLanguageUiRenderer(ui)

  @Test
  fun `when the model is being initialized, do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when the current language has not been fetched, do nothing`() {
    // given
    val model = defaultModel
        .withSupportedLanguages(listOf(englishIndia, hindiIndia))

    // when
    renderer.render(model)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `when the supported languages have not been fetched, do nothing`() {
    // given
    val model = defaultModel
        .withCurrentLanguage(englishIndia)

    // when
    renderer.render(model)

    // then
    verifyZeroInteractions(ui)
  }

  @Test
  fun `if the user has selected a language, render the ui with the selected language`() {
    // given
    val model = defaultModel
        .withSupportedLanguages(supportedLanguages)
        .withCurrentLanguage(englishIndia)

    // when
    renderer.render(model)

    // then
    verify(ui).displayLanguages(supportedLanguages, englishIndia)
    verify(ui).setDoneButtonEnabled()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `if the user is using the system default language, render the ui without any selected language`() {
    // given
    val model = defaultModel
        .withCurrentLanguage(SystemDefaultLanguage)
        .withSupportedLanguages(supportedLanguages)

    // when
    renderer.render(model)

    // then
    verify(ui).displayLanguages(supportedLanguages, null)
    verify(ui).setDoneButtonDisabled()
    verifyNoMoreInteractions(ui)
  }
}
