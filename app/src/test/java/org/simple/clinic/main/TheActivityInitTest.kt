package org.simple.clinic.main

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.mobius.CustomFirstMatchers.doesNotHaveEffectOfType

class TheActivityInitTest {

  private val spec = InitSpec(TheActivityInit())

  @Test
  fun `when the screen is created for an already logged in user, the screen lock must be verified`() {
    val model = TheActivityModel.createForAlreadyLoggedInUser()

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(LoadAppLockInfo)
        ))
  }

  @Test
  fun `when the screen is created for an freshly logged in user, the screen lock must not be verified`() {
    val model = TheActivityModel.createForNewlyLoggedInUser()

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            doesNotHaveEffectOfType(LoadAppLockInfo::class.java)
        ))
  }
}
