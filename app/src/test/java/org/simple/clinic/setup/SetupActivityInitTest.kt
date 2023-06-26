package org.simple.clinic.setup

import com.spotify.mobius.test.FirstMatchers.hasEffects
import com.spotify.mobius.test.FirstMatchers.hasModel
import com.spotify.mobius.test.InitSpec
import com.spotify.mobius.test.InitSpec.assertThatFirst
import org.junit.Test
import org.simple.sharedTestCode.util.TestUtcClock
import java.time.Instant

class SetupActivityInitTest {

  @Test
  fun `when the screen is created and database encryption is enabled, then execute database encryption`() {
    val spec = InitSpec(SetupActivityInit(isDatabaseEncryptionEnabled = true))
    val model = SetupActivityModel.create(TestUtcClock(Instant.parse("2018-01-01T00:00:00Z")))

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(ExecuteDatabaseEncryption)
        ))
  }

  @Test
  fun `when the screen is created and database encryption is disabled, then check if app can run`() {
    val spec = InitSpec(SetupActivityInit(isDatabaseEncryptionEnabled = false))
    val model = SetupActivityModel.create(TestUtcClock(Instant.parse("2018-01-01T00:00:00Z")))

    spec
        .whenInit(model)
        .then(assertThatFirst(
            hasModel(model),
            hasEffects(CheckIfAppCanRun)
        ))
  }
}
