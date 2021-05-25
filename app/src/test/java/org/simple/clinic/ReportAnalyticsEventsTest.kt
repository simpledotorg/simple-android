package org.simple.clinic

import com.google.common.truth.Truth.assertThat
import io.reactivex.subjects.PublishSubject
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.simple.clinic.analytics.MockAnalyticsReporter
import org.simple.clinic.analytics.MockAnalyticsReporter.Event
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.util.RxErrorsRule
import org.simple.clinic.widgets.UiEvent

class ReportAnalyticsEventsTest {

  @get:Rule
  val rxErrorsRule = RxErrorsRule()

  private data class UiEvent1(val prop: String) : UiEvent {
    override val analyticsName = "UiEvent 1"
  }

  private data class UiEvent2(val prop: Int) : UiEvent {
    override val analyticsName = "UiEvent 2"
  }

  private data class UiEvent3(val prop: String) : UiEvent

  private val uiEvents = PublishSubject.create<UiEvent>()

  private val reporter = MockAnalyticsReporter()

  private lateinit var controller: ReportAnalyticsEvents

  private val forwardedEvents = mutableListOf<UiEvent>()

  @Before
  fun setUp() {
    Analytics.addReporter(reporter)
    controller = ReportAnalyticsEvents()
    uiEvents.compose(controller).subscribe { forwardedEvents.add(it) }
  }

  @After
  fun tearDown() {
    forwardedEvents.clear()
    reporter.clear()
    Analytics.clearReporters()
  }

  @Test
  fun `whenever ui events are emitted, their analytics name must be emitted to the reporters`() {
    uiEvents.onNext(UiEvent1("1"))
    uiEvents.onNext(UiEvent2(2))
    uiEvents.onNext(UiEvent1("3"))

    val expected = listOf(
        Event("UserInteraction", mapOf("name" to "UiEvent 1")),
        Event("UserInteraction", mapOf("name" to "UiEvent 2")),
        Event("UserInteraction", mapOf("name" to "UiEvent 1"))
    )

    assertThat(reporter.receivedEvents).isEqualTo(expected)
  }

  @Test
  fun `whenever ui events are emitted, the events must be forwarded to the controller`() {
    uiEvents.onNext(UiEvent1("1"))
    uiEvents.onNext(UiEvent2(2))
    uiEvents.onNext(UiEvent1("3"))

    assertThat(forwardedEvents).isEqualTo(listOf(UiEvent1("1"), UiEvent2(2), UiEvent1("3")))
  }

  @Test
  fun `whenever any ui event analytics name is blank, it should not report the event`() {
    uiEvents.onNext(UiEvent1("1"))
    uiEvents.onNext(UiEvent3("2"))
    uiEvents.onNext(UiEvent1("3"))

    val expectedEvents = listOf(
        Event("UserInteraction", mapOf("name" to "UiEvent 1")),
        Event("UserInteraction", mapOf("name" to "UiEvent 1"))
    )

    assertThat(reporter.receivedEvents).isEqualTo(expectedEvents)
  }

  @Test
  fun `whenever any ui event analytics name is blank, it should forward them to the controller`() {
    uiEvents.onNext(UiEvent1("1"))
    uiEvents.onNext(UiEvent3("2"))
    uiEvents.onNext(UiEvent1("3"))

    assertThat(forwardedEvents).isEqualTo(listOf(UiEvent1("1"), UiEvent3("2"), UiEvent1("3")))
  }
}
