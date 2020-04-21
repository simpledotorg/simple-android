package org.simple.clinic.settings

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class SettingsInitTest {

  private val defaultModel = SettingsModel.default(applicationId = "org.simple")

  private val spec = InitSpec<SettingsModel, SettingsEffect>(SettingsInit())

  @Test
  fun `when screen is created, then user details, then load initial data`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadUserDetailsEffect, LoadCurrentLanguageEffect, LoadAppVersionEffect(defaultModel.applicationId), CheckAppUpdateAvailable)
        ))
  }

  @Test
  fun `when the screen is restored, then load initial data`() {
    val restoredModel = defaultModel
        .userDetailsFetched(name = "Anish Acharya", phoneNumber = "1234567890")
        .currentLanguageFetched(ProvidedLanguage(displayName = "English", languageCode = "en-IN"))
        .appVersionLoaded("1.0.0")

    spec
        .whenInit(restoredModel)
        .then(assertThatFirst(
            hasModel(restoredModel),
            hasEffects(LoadCurrentLanguageEffect as SettingsEffect)
        ))
  }
}
