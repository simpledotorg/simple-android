package org.simple.clinic.setup

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test

class SetupActivityInitTest {

  @Test
  fun `when the screen is created, the database must be initialized`() {
    // given
    val spec = InitSpec(SetupActivityInit())
    val model = SetupActivityModel.create()

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(InitializeDatabase as SetupActivityEffect)
        ))
  }
}
