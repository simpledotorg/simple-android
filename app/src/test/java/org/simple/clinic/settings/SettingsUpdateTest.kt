package org.simple.clinic.settings

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class SettingsUpdateTest {

  private val defaultModel = SettingsModel.FETCHING_USER_DETAILS

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
}
