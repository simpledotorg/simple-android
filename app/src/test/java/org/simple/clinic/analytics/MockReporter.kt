package org.simple.clinic.analytics

class MockReporter : Reporter {

  val receivedEvents = mutableListOf<Pair<String, Map<String, Any>>>()
  val setProperties = mutableMapOf<String, Any>()

  override fun createEvent(event: String, props: Map<String, Any>) {
    receivedEvents.add(event to props)
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
}
