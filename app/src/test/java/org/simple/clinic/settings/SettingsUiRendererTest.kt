package org.simple.clinic.settings

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.Test

class SettingsUiRendererTest {

  private val ui = mock<SettingsUi>()
  private val renderer = SettingsUiRenderer(ui)
  private val defaultModel = SettingsModel.default(applicationId = "org.simple")

  @Test
  fun `when the user details are being fetched, do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user details are fetched, render them on the UI`() {
    // given
    val name = "Anish Acharya"
    val phoneNumber = "1234567890"
    val model = defaultModel.userDetailsFetched(name, phoneNumber)

    // when
    renderer.render(model)

    // then
    verify(ui).displayUserDetails(name, phoneNumber)
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the current language is the system language, do not render it on the UI`() {
    // given
    val model = defaultModel.currentLanguageFetched(SystemDefaultLanguage)

    // when
    renderer.render(model)

    // then
    verify(ui, never()).displayCurrentLanguage(any())
    verify(ui).setChangeLanguageButtonVisible()
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the current language is a manually selected language, render it on the UI`() {
    // given
    val language = ProvidedLanguage(displayName = "English", languageCode = "en-IN")
    val model = defaultModel.currentLanguageFetched(language)

    // when
    renderer.render(model)

    // then
    verify(ui).displayCurrentLanguage(language.displayName)
    verify(ui).setChangeLanguageButtonVisible()
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the app version is loaded, render them on the UI`() {
    // given
    val appVersion = "1.0.0"
    val model = defaultModel.appVersionLoaded(appVersion)

    // when
    renderer.render(model)

    // then
    verify(ui).displayAppVersion(appVersion)
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the app update is available then show update button`() {
    // given
    val model = defaultModel.checkedAppUpdate(true)

    // when
    renderer.render(model)

    // then
    verify(ui).showAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the app update is not available then don't show update button`() {
    // given
    val model = defaultModel.checkedAppUpdate(false)

    // when
    renderer.render(model)

    // then
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }
}
