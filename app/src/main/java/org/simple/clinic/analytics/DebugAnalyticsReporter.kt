package org.simple.clinic.analytics

import timber.log.Timber

class DebugAnalyticsReporter : AnalyticsReporter {

  override fun setUserIdentity(id: String) {
    Timber.tag("Analytics").d("User ID: $id")
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    Timber.tag("Analytics").d("Event: $event -> $props")
  }

  override fun setProperty(key: String, value: Any) {
    Timber.tag("Analytics").d("Prop: $key -> $value")
  }
}
