package org.simple.clinic.registerorlogin

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class AuthenticationInitTest {

  private val spec = InitSpec(AuthenticationInit())

  @Test
  fun `when the screen is created for a new login, open the country selection screen`() {
    val model = AuthenticationModel.create(OpenFor.NewLogin)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(OpenCountrySelectionScreen)
        ))
  }

  @Test
  fun `when the screen is created for a reauthentication, open the phone number entry screen`() {
    val model = AuthenticationModel.create(OpenFor.Reauthentication)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(OpenRegistrationPhoneScreen)
        ))
  }
}
