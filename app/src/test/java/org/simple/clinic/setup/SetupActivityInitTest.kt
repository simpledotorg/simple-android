package org.simple.clinic.setup

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class SetupActivityInitTest {

  @Test
  fun `when the screen is created, the user details must be loaded`() {
    // given
    val spec = InitSpec(SetupActivityInit())

    spec
        .whenInit(SetupActivityModel)
        .then(assertThatFirst(
            hasModel(SetupActivityModel),
            hasEffects(FetchUserDetails as SetupActivityEffect)
        ))
  }
}
