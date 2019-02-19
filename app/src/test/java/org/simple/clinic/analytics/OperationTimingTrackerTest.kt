package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.simple.clinic.util.TestUtcClock
import org.threeten.bp.Duration
import org.threeten.bp.Instant

class OperationTimingTrackerTest {

  val clock = TestUtcClock()

  val name = "Test Operation"

  val timingTracker = OperationTimingTracker(
      name = name,
      clock = clock)

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }

  @Test
  fun `starting an operation must record it with the time`() {
    val now = Instant.now(clock)

    timingTracker.start("Sub Operation 1")
    clock.advanceBy(Duration.ofHours(1L))
    timingTracker.start("Sub Operation 2")
    clock.advanceBy(Duration.ofDays(5L))
    timingTracker.start("Sub Operation 3")

    val expected = mapOf(
        "$name:Sub Operation 1" to now,
        "$name:Sub Operation 2" to now.plus(Duration.ofHours(1L)),
        "$name:Sub Operation 3" to now.plus(Duration.ofHours(1L).plusDays(5L))
    )
    assertThat(timingTracker.ongoing).isEqualTo(expected)
  }

  @Test
  fun `stopping an operation must report it to analytics with the time`() {
    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    val stage1 = "Sub Operation 1"
    val stage2 = "Sub Operation 2"
    val stage3 = "Sub Operation 3"
    val stage1AnalyticsName = "$name:$stage1"
    val stage2AnalyticsName = "$name:$stage2"
    val stage3AnalyticsName = "$name:$stage3"

    val operation1StartedAt = Instant.now(clock)
    timingTracker.start(stage1)
    clock.advanceBy(Duration.ofMinutes(1L))
    val operation2StartedAt = Instant.now(clock)
    timingTracker.start(stage2)
    clock.advanceBy(Duration.ofHours(2L))
    val operation3StartedAt = Instant.now(clock)
    timingTracker.start(stage3)

    assertThat(timingTracker.ongoing.keys)
        .isEqualTo(setOf(stage1AnalyticsName, stage2AnalyticsName, stage3AnalyticsName))

    timingTracker.stop(stage1)
    assertThat(timingTracker.ongoing.keys)
        .isEqualTo(setOf(stage2AnalyticsName, stage3AnalyticsName))
    val operation1StoppedAt = operation1StartedAt
        .plus(Duration
            .ofMinutes(1L)
            .plusHours(2L))
    val timeTakenForOperation1 = Duration.between(operation1StartedAt, operation1StoppedAt)

    clock.advanceBy(Duration.ofMinutes(1L))
    timingTracker.stop(stage3)
    assertThat(timingTracker.ongoing.keys)
        .isEqualTo(setOf(stage2AnalyticsName))
    val operation3StoppedAt = operation1StartedAt
        .plus(Duration
            .ofMinutes(1L)
            .plusHours(2L)
            .plusMinutes(1L))
    val timeTakenForOperation3 = Duration.between(operation3StartedAt, operation3StoppedAt)

    clock.advanceBy(Duration.ofMinutes(30L))
    timingTracker.stop(stage2)
    assertThat(timingTracker.ongoing.keys).isEmpty()
    val operation2StoppedAt = operation1StartedAt.plus(
        Duration
            .ofMinutes(1L)
            .plusHours(2L)
            .plusMinutes(1L)
            .plusMinutes(30L))
    val timeTakenForOperation2 = Duration.between(operation2StartedAt, operation2StoppedAt)

    val expected = listOf(
        MockAnalyticsReporter.Event(
            name = "TimeTaken",
            props = mapOf(
                "operationName" to stage1AnalyticsName,
                "timeTakenInMillis" to timeTakenForOperation1.toMillis()
            )
        ),
        MockAnalyticsReporter.Event(
            name = "TimeTaken",
            props = mapOf(
                "operationName" to stage3AnalyticsName,
                "timeTakenInMillis" to timeTakenForOperation3.toMillis()
            )
        ),
        MockAnalyticsReporter.Event(
            name = "TimeTaken",
            props = mapOf(
                "operationName" to stage2AnalyticsName,
                "timeTakenInMillis" to timeTakenForOperation2.toMillis()
            )
        )
    )

    assertThat(reporter.receivedEvents).isEqualTo(expected)
  }

  @Test
  fun `stopping a stage that does not exist must not fail`() {
    val reporter = MockAnalyticsReporter()
    Analytics.addReporter(reporter)

    assertThat(timingTracker.ongoing).isEmpty()
    timingTracker.stop("Operation")

    assertThat(reporter.receivedEvents).isEmpty()
  }

  @Test
  fun `starting a stage that already exists must replace the older started stage`() {
    val now = Instant.now(clock)

    val stageName = "Sub Operation"
    timingTracker.start(stageName)
    val advanceClockBy = Duration.ofHours(1L)
    clock.advanceBy(advanceClockBy)
    timingTracker.start(stageName)

    assertThat(timingTracker.ongoing["$name:$stageName"])
        .isEqualTo(now.plus(advanceClockBy))
  }
}
