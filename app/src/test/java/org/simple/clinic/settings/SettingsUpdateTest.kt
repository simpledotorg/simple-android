package org.simple.clinic.settings

import com.spotify.mobius.test.NextMatchers.hasEffects
import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.NextMatchers.hasNoModel
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserSession.LogoutResult.Failure
import org.simple.clinic.user.UserSession.LogoutResult.Success

class SettingsUpdateTest {

  private val defaultModel = SettingsModel.default()

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

  @Test
  fun `when logout button is clicked, then show confirm logout dialog`() {
    spec
        .given(defaultModel)
        .whenEvent(LogoutButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(ShowConfirmLogoutDialog)
        ))
  }

  @Test
  fun `when confirm logout button is clicked, then logout user`() {
    spec
        .given(defaultModel)
        .whenEvent(ConfirmLogoutButtonClicked)
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(LogoutUser)
        ))
  }

  @Test
  fun `when user is logged out successfully, then restart the app process`() {
    spec
        .given(defaultModel)
        .whenEvent(UserLogoutResult(Success))
        .then(assertThatNext(
            hasNoModel(),
            hasEffects(RestartApp)
        ))
  }

  @Test
  fun `when user is not logged out successfully, then do nothing`() {
    spec
        .given(defaultModel)
        .whenEvent(UserLogoutResult(Failure(IllegalArgumentException())))
        .then(assertThatNext(
            hasNoModel(),
            hasNoEffects()
        ))
  }
}
