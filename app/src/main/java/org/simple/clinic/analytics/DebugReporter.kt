package org.simple.clinic.analytics

import timber.log.Timber

class DebugReporter : Reporter {

  override fun createEvent(event: String, props: Map<String, Any>) {
    Timber.tag("Analytics").d("Event: $event -> $props")
  }

  override fun setProperty(key: String, value: Any) {
    Timber.tag("Analytics").d("Prop: $key -> $value")
  }
}
