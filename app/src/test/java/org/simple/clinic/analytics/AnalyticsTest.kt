package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test

class AnalyticsTest {

  @Test
  fun `when reporting interaction events without any reporters, no error should be thrown`() {
    Analytics.reportUserInteraction("Test")
  }

  @Test
  fun `when a reporter fails when sending interaction events, no error should be thrown`() {
    Analytics.addReporter(object : Reporter {
      override fun createEvent(event: String, props: Map<String, Any>) {
        throw RuntimeException()
      }

      override fun setProperty(key: String, value: Any) {
        throw RuntimeException()
      }
    })
    Analytics.reportUserInteraction("Test")
  }

  @Test
  fun `when multiple reporters are present and one throws an error, the others should receive the events`() {
    val reporter1 = MockReporter()
    val reporter2 = object : Reporter {
      override fun createEvent(event: String, props: Map<String, Any>) {
        throw RuntimeException()
      }

      override fun setProperty(key: String, value: Any) {
        throw RuntimeException()
      }
    }
    val reporter3 = MockReporter()

    Analytics.addReporter(reporter1, reporter2, reporter3)

    Analytics.reportUserInteraction("Test 1")
    Analytics.reportUserInteraction("Test 2")
    Analytics.reportUserInteraction("Test 3")

    val expected = listOf<Pair<String, Map<String, Any>>>(
        "UserInteraction" to mapOf("name" to "Test 1"),
        "UserInteraction" to mapOf("name" to "Test 2"),
        "UserInteraction" to mapOf("name" to "Test 3")
    )

    assertThat(reporter1.receivedEvents).isEqualTo(expected)
    assertThat(reporter3.receivedEvents).isEqualTo(expected)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }
}
