package org.simple.clinic.settings

import com.spotify.mobius.test.NextMatchers.hasModel
import com.spotify.mobius.test.NextMatchers.hasNoEffects
import com.spotify.mobius.test.UpdateSpec
import com.spotify.mobius.test.UpdateSpec.assertThatNext
import org.junit.Test

class SettingsUpdateTest {

  private val defaultModel = SettingsModel.FETCHING_USER_DETAILS

  @Test
  fun `the user details loaded event must update the model`() {
    val spec = UpdateSpec<SettingsModel, SettingsEvent, SettingsEffect>(SettingsUpdate())
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
}
