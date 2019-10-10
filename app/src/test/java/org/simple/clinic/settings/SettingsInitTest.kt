package org.simple.clinic.settings

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.FirstMatchers.hasNoEffects
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class SettingsInitTest {

  private val defaultModel = SettingsModel.FETCHING_USER_DETAILS

  private val spec = InitSpec<SettingsModel, SettingsEffect>(SettingsInit())

  @Test
  fun `the load user effect must be emitted when the model is created`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadUserDetailsEffect as SettingsEffect)
        ))
  }

  @Test
  fun `no effect must be emitted when the model is restored`() {
    val restoredModel = defaultModel.userDetailsFetched(name = "Anish Acharya", phoneNumber = "1234567890")

    spec
        .whenInit(restoredModel)
        .then(assertThatFirst(
            hasModel(restoredModel),
            hasNoEffects()
        ))
  }
}
