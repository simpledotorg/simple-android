package org.simple.clinic.analytics

import timber.log.Timber

class DebugAnalyticsReporter : AnalyticsReporter {

  override fun setUserIdentity(id: String) {
    Timber.tag("Analytics").d("User ID: $id")
  }

  override fun resetUser() {
    Timber.tag("Analytics").d("Reset User ID")
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    Timber.tag("Analytics").d("Event: $event -> $props")
  }
}
