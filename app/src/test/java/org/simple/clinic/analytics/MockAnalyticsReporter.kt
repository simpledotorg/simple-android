package org.simple.clinic.analytics

class MockAnalyticsReporter : AnalyticsReporter {

  val setUserIds = mutableListOf<String>()
  val receivedEvents = mutableListOf<Event>()

  override fun setUserIdentity(id: String) {
    setUserIds.add(id)
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    receivedEvents.add(Event(event, props))
  }

  fun clearReceivedEvents() {
    receivedEvents.clear()
  }

  fun clearSetUserIds() {
    setUserIds.clear()
  }

  fun clear() {
    clearReceivedEvents()
    clearSetUserIds()
  }

  data class Event(val name: String, val props: Map<String, Any>)
}
