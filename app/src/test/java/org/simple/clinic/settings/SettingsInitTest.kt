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
  fun `when the screen is created, the user details must be fetched`() {
    spec
        .whenInit(defaultModel)
        .then(assertThatFirst(
            hasModel(defaultModel),
            hasEffects(LoadUserDetailsEffect as SettingsEffect)
        ))
  }

  @Test
  fun `when the screen is restored with the user details, do nothing`() {
    val restoredModel = defaultModel.userDetailsFetched(name = "Anish Acharya", phoneNumber = "1234567890")

    spec
        .whenInit(restoredModel)
        .then(assertThatFirst(
            hasModel(restoredModel),
            hasNoEffects()
        ))
  }
}
