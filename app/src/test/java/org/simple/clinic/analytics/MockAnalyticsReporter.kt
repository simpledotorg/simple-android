package org.simple.clinic.analytics

import org.simple.clinic.user.User

class MockAnalyticsReporter : AnalyticsReporter {

  var userId: String? = null
  var user: User? = null
  val receivedEvents = mutableListOf<Event>()

  override fun setUserIdentity(id: String) {
    userId = id
  }

  override fun setLoggedInUser(user: User) {
    this.user = user
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    receivedEvents.add(Event(event, props))
  }

  override fun resetUser() {
    userId = null
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
    userId = null
    user = null
  }

  data class Event(val name: String, val props: Map<String, Any>)
}
