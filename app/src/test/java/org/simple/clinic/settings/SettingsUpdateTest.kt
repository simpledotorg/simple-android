package org.simple.clinic.settings

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class SettingsUpdateTest {

  private val defaultModel = SettingsModel.default(applicationId = "org.simple")

  private val spec = UpdateSpec<SettingsModel, SettingsEvent, SettingsEffect>(SettingsUpdate())

  @Test
  fun `when the user details are loaded, the ui must be updated`() {
    val userName = "Anish Acharya"
    val userPhoneNumber = "1234567890"

    spec
        .given(defaultModel)
        .whenEvent(UserDetailsLoaded(userName, userPhoneNumber))
        .then(assertThatNext(
            hasModel(defaultModel.userDetailsFetched(userName, userPhoneNumber)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the current language is loaded, the ui must be updated`() {
    val language = ProvidedLanguage(displayName = "English", languageCode = "en-IN")

    spec
        .given(defaultModel)
        .whenEvent(CurrentLanguageLoaded(language))
        .then(assertThatNext(
            hasModel(defaultModel.currentLanguageFetched(language)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when the change language button is clicked, the language selection screen must be opened`() {
    val model = defaultModel
        .userDetailsFetched("Anish Acharya", "1234567890")
        .currentLanguageFetched(SystemDefaultLanguage)

    spec
        .given(model)
        .whenEvent(ChangeLanguage)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(OpenLanguageSelectionScreenEffect as SettingsEffect)
        ))
  }

  @Test
  fun `when the app version is loaded, then ui must be updated`() {
    val appVersion = "1.0.0"

    spec
        .given(defaultModel)
        .whenEvent(AppVersionLoaded(appVersion))
        .then(assertThatNext(
            hasModel(defaultModel.appVersionLoaded(appVersion)),
            hasNoEffects()
        ))
  }

  @Test
  fun `when there is app update, then ui must be updated`() {
    val isUpdateAvailable = true

    spec
        .given(defaultModel)
        .whenEvent(AppUpdateAvailabilityChecked(isUpdateAvailable))
        .then(assertThatNext(
            hasModel(defaultModel.checkedAppUpdate(isUpdateAvailable))
        ))
  }
}
