package org.simple.clinic.main

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class TheActivityInitTest {

  private val spec = InitSpec(TheActivityInit())

  @Test
  fun `when the screen is created, the screen lock must be verified`() {
    val model = TheActivityModel.createForAlreadyLoggedInUser()

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadAppLockInfo)
        ))
  }
}
