package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import io.reactivex.schedulers.TestScheduler
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.simple.clinic.util.advanceTimeBy
import org.threeten.bp.Duration

class RxTimingAnalyticsTest {

  private val reporter = MockAnalyticsReporter()
  private val timestampScheduler = TestScheduler()
  private val analyticsName = "Event"
  private val events = PublishSubject.create<Any>()

  private val rxTimingAnalytics = RxTimingAnalytics<Any>(
      analyticsName = analyticsName,
      timestampScheduler = timestampScheduler
  )

  @Before
  fun setUp() {
    Analytics.addReporter(reporter)
    events
        .compose(rxTimingAnalytics)
        .subscribe()
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }


  @Test
  fun `time taken for the first emission should be reported to analytics`() {
    // when
    val operationTime = Duration.ofHours(1L)
    timestampScheduler.advanceTimeBy(operationTime)
    events.onNext(Any())

    // then
    assertThat(reporter.receivedEvents)
        .isEqualTo(listOf(
            MockAnalyticsReporter.Event(
                name = "TimeTaken",
                props = mapOf(
                    "operationName" to analyticsName,
                    "timeTakenInMillis" to operationTime.toMillis()
                )
            )
        ))
  }

  @Test
  fun `subsequent emissions should be ignored for reporting to analytics`() {
    // when
    val firstOperationTime = Duration.ofHours(1L)
    val secondOperationTime = Duration.ofMinutes(15L)

    timestampScheduler.advanceTimeBy(firstOperationTime)
    events.onNext(Any())
    timestampScheduler.advanceTimeBy(secondOperationTime)
    events.onNext(Any())

    // then
    assertThat(reporter.receivedEvents)
        .isEqualTo(listOf(
            MockAnalyticsReporter.Event(
                name = "TimeTaken",
                props = mapOf(
                    "operationName" to analyticsName,
                    "timeTakenInMillis" to firstOperationTime.toMillis()
                )
            )
        ))
  }
}
