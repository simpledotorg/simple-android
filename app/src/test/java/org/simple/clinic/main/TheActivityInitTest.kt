package org.simple.clinic.main

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class TheActivityInitTest {

  private val spec = InitSpec(TheActivityInit())

  @Test
  fun `when the screen is created, the info required to decide the initial screen must be loaded`() {
    val model = TheActivityModel.createForAlreadyLoggedInUser()

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadInitialScreenInfo)
        ))
  }
}
