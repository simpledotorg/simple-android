package org.simple.clinic.settings

import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoMoreInteractions
import org.junit.Test

class SettingsUiRendererTest {

  private val ui = mock<SettingsUi>()
  private val renderer = SettingsUiRenderer(ui)
  private val defaultModel = SettingsModel.default()

  @Test
  fun `when the user details are being fetched, do nothing`() {
    // when
    renderer.render(defaultModel)

    // then
    verify(ui).hideAppUpdateButton()
    verify(ui).hideLoggingOutProgressIndicator()
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
    verify(ui).hideLoggingOutProgressIndicator()
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
    verify(ui).hideLoggingOutProgressIndicator()
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
    verify(ui).hideLoggingOutProgressIndicator()
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
    verify(ui).hideLoggingOutProgressIndicator()
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
    verify(ui).hideLoggingOutProgressIndicator()
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
    verify(ui).hideLoggingOutProgressIndicator()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when the user is logging out, then show the progress indicator`() {
    // given
    val model = defaultModel.userLoggingOut()

    // when
    renderer.render(model)

    // then
    verify(ui).showLoggingOutProgressIndicator()
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when user logging out is finished, then hide the progress indicator`() {
    // given
    val model = defaultModel.userLoggedOut()

    // when
    renderer.render(model)

    // then
    verify(ui).hideLoggingOutProgressIndicator()
    verify(ui).hideAppUpdateButton()
    verifyNoMoreInteractions(ui)
  }

  @Test
  fun `when database is encrypted and app version is loaded, then show the status icon`() {
    // given
    val appVersion = "1.0.0"
    val model = defaultModel
        .databaseEncryptionStatusLoaded(isDatabaseEncrypted = true)
        .appVersionLoaded(appVersion)

    // when
    renderer.render(model)

    // then
    verify(ui).displayAppVersion(appVersion)
    verify(ui).hideAppUpdateButton()
    verify(ui).hideLoggingOutProgressIndicator()
    verify(ui).displayAppSecureIcon()
    verifyNoMoreInteractions(ui)
  }
}
