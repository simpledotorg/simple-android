package org.simple.clinic.analytics

import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.simple.clinic.analytics.MockReporter.Event

class AnalyticsTest {

  @Test
  fun `when reporting interaction events without any reporters, no error should be thrown`() {
    Analytics.reportUserInteraction("Test")
  }

  @Test
  fun `when reporting screen change events without any reporters, no error should be thrown`() {
    Analytics.reportScreenChange("Screen 1", "Screen 2")
  }

  @Test
  fun `when reporting input validation errors without any reporters, no error should be thrown`() {
    Analytics.reportInputValidationError("Error")
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
  fun `when a reporter fails when sending screen change events, no error should be thrown`() {
    Analytics.addReporter(object : Reporter {
      override fun createEvent(event: String, props: Map<String, Any>) {
        throw RuntimeException()
      }

      override fun setProperty(key: String, value: Any) {
        throw RuntimeException()
      }
    })
    Analytics.reportScreenChange("Screen 1", "Screen 2")
  }

  @Test
  fun `when a reporter fails when sending validation error events, no error should be thrown`() {
    Analytics.addReporter(object : Reporter {
      override fun createEvent(event: String, props: Map<String, Any>) {
        throw RuntimeException()
      }

      override fun setProperty(key: String, value: Any) {
        throw RuntimeException()
      }
    })
    Analytics.reportInputValidationError("Error")
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
    Analytics.reportScreenChange("Screen 1", "Screen 2")
    Analytics.reportInputValidationError("Error 1")
    Analytics.reportInputValidationError("Error 2")

    val expected = listOf(
        Event("UserInteraction", mapOf("name" to "Test 1")),
        Event("UserInteraction", mapOf("name" to "Test 2")),
        Event("UserInteraction", mapOf("name" to "Test 3")),
        Event("ScreenChange", mapOf("outgoing" to "Screen 1", "incoming" to "Screen 2")),
        Event("InputValidationError", mapOf("name" to "Error 1")),
        Event("InputValidationError", mapOf("name" to "Error 2"))
    )

    assertThat(reporter1.receivedEvents).isEqualTo(expected)
    assertThat(reporter3.receivedEvents).isEqualTo(expected)
  }

  @After
  fun tearDown() {
    Analytics.clearReporters()
  }
}
