package org.simple.clinic.analytics

import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.analytics.AnalyticsUser

class MockAnalyticsReporter : AnalyticsReporter {

  var user: AnalyticsUser? = null
  var isANewRegistration: Boolean? = null
  val receivedEvents = mutableListOf<Event>()

  override fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean) {
    this.user = user
    this.isANewRegistration = isANewRegistration
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    receivedEvents.add(Event(event, props))
  }

  override fun resetUser() {
    user = null
  }

  fun clear() {
    clearReceivedEvents()
    clearUsers()
  }

  private fun clearReceivedEvents() {
    receivedEvents.clear()
  }

  private fun clearUsers() {
    user = null
  }

  data class Event(val name: String, val props: Map<String, Any>)
}
