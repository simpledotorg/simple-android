package org.simple.clinic.location

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.simple.clinic.util.TestElapsedRealtimeClock
import java.time.Duration

class LocationUpdateTest {

  @Test
  fun `age of location update should be calculated correctly`() {
    val elapsedRealtimeClock = TestElapsedRealtimeClock()

    val update = LocationUpdate.Available(
        location = Coordinates(0.0, 0.0),
        timeSinceBootWhenRecorded = Duration.ofMillis(elapsedRealtimeClock.millis()))

    val advancement = Duration.ofHours(2)
    elapsedRealtimeClock.advanceBy(advancement)

    val age = update.age(elapsedRealtimeClock)
    assertThat(age).isEqualTo(advancement)
  }
}
