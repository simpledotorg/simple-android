package org.simple.clinic.setup

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.clinic.util.TestUtcClock
import java.time.Instant

class SetupActivityInitTest {

  @Test
  fun `when the screen is created, the database must be initialized`() {
    // given
    val spec = InitSpec(SetupActivityInit())
    val model = SetupActivityModel.create(TestUtcClock(Instant.parse("2018-01-01T00:00:00Z")))

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(InitializeDatabase as SetupActivityEffect)
        ))
  }
}
