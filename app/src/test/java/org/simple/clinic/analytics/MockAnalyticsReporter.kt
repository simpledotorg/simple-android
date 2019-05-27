package org.simple.clinic.analytics

class MockAnalyticsReporter : AnalyticsReporter {

  var userId: String? = null
  val receivedEvents = mutableListOf<Event>()

  override fun setUserIdentity(id: String) {
    userId = id
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    receivedEvents.add(Event(event, props))
  }

  override fun resetUserIdentity() {
    userId = null
  }

  fun clear() {
    clearReceivedEvents()
    clearUserIds()
  }

  private fun clearReceivedEvents() {
    receivedEvents.clear()
  }

  private fun clearUserIds() {
    userId = null
  }

  data class Event(val name: String, val props: Map<String, Any>)
}
