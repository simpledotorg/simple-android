package org.simple.clinic.analytics

class MockAnalyticsReporter : AnalyticsReporter {

  var setUserId: String? = null
  val receivedEvents = mutableListOf<Event>()

  override fun setUserIdentity(id: String) {
    setUserId = id
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    receivedEvents.add(Event(event, props))
  }

  override fun resetUserIdentity() {
    setUserId = null
  }

  fun clear() {
    clearReceivedEvents()
    clearSetUserIds()
  }

  private fun clearReceivedEvents() {
    receivedEvents.clear()
  }

  private fun clearSetUserIds() {
    setUserId = null
  }

  data class Event(val name: String, val props: Map<String, Any>)
}
