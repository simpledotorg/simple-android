package org.simple.clinic.settings

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class SettingsInitTest {

  private val defaultModel = SettingsModel.FETCHING_USER_DETAILS

  private val spec = InitSpec<SettingsModel, SettingsEffect>(SettingsInit())

  @Test
  fun `when the screen is created, the user details and current language must be fetched`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadUserDetailsEffect, LoadCurrentLanguageEffect)
        ))
  }

  @Test
  fun `when the screen is restored, the current language must be fetched`() {
    val restoredModel = defaultModel
        .userDetailsFetched(name = "Anish Acharya", phoneNumber = "1234567890")
        .currentLanguageFetched(ProvidedLanguage(displayName = "English", languageCode = "en-IN"))

    spec
        .whenInit(restoredModel)
        .then(assertThatFirst(
            hasModel(restoredModel),
            hasEffects(LoadCurrentLanguageEffect as SettingsEffect)
        ))
  }
}
