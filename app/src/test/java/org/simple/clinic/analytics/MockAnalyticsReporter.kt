package org.simple.clinic.analytics

class MockAnalyticsReporter : AnalyticsReporter {

  val setUserIds = mutableListOf<String>()
  val receivedEvents = mutableListOf<Event>()
  val setProperties = mutableMapOf<String, Any>()

  override fun setUserIdentity(id: String) {
    setUserIds.add(id)
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    receivedEvents.add(Event(event, props))
  }

  override fun setProperty(key: String, value: Any) {
    setProperties[key] = value
  }

  fun clearReceivedEvents() {
    receivedEvents.clear()
  }

  fun clearSetProperties() {
    setProperties.clear()
  }

  fun clearSetUserIds() {
    setUserIds.clear()
  }

  fun clear() {
    clearReceivedEvents()
    clearSetProperties()
    clearSetUserIds()
  }

  data class Event(val name: String, val props: Map<String, Any>)
}
