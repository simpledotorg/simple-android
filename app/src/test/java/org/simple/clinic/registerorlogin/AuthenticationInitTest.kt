package org.simple.clinic.registerorlogin

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class AuthenticationInitTest {

  @Test
  fun `when the screen is created for a new login, open the country selection screen`() {
    val spec = InitSpec(AuthenticationInit())
    val model = AuthenticationModel.create(OpenFor.NewLogin)

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(OpenCountrySelectionScreen)
        ))
  }
}
