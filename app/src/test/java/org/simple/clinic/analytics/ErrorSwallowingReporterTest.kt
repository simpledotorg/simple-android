package org.simple.clinic.analytics

import org.junit.Test
import org.simple.clinic.TestData
import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.analytics.AnalyticsUser

class ErrorSwallowingReporterTest {

  private val reporter = ErrorSwallowingReporter(FailingAnalyticsReporter())

  @Test(expected = Test.None::class)
  fun `no error should be thrown when reporting a new user registration`() {
    // given
    val user = TestData.loggedInUser()

    // when
    reporter.setLoggedInUser(AnalyticsUser(user.uuid, user.fullName), isANewRegistration = true)
  }

  @Test(expected = Test.None::class)
  fun `no error should be thrown when reporting a user login`() {
    // given
    val user = TestData.loggedInUser()

    // when
    reporter.setLoggedInUser(AnalyticsUser(user.uuid, user.fullName), isANewRegistration = false)
  }

  @Test(expected = Test.None::class)
  fun `no error should be thrown when clearing the user`() {
    // when
    reporter.resetUser()
  }

  @Test(expected = Test.None::class)
  fun `no error should be thrown when creating an event`() {
    // when
    reporter.createEvent("Event", emptyMap())
  }

  private class FailingAnalyticsReporter : AnalyticsReporter {

    override fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean) {
      throw RuntimeException()
    }

    override fun resetUser() {
      throw RuntimeException()
    }

    override fun createEvent(event: String, props: Map<String, Any>) {
      throw RuntimeException()
    }
  }
}
