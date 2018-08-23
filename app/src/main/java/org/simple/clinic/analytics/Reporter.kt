package org.simple.clinic.analytics

interface Reporter {

  fun createEvent(event: String, props: Map<String, Any>)

  fun setProperty(key: String, value: Any)
}
