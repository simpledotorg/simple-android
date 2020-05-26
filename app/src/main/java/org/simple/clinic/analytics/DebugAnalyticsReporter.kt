package org.simple.clinic.analytics

import org.simple.clinic.platform.analytics.AnalyticsReporter
import org.simple.clinic.platform.analytics.AnalyticsUser
import timber.log.Timber

class DebugAnalyticsReporter : AnalyticsReporter {

  override fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean) {
    Timber.tag("Analytics").d("User: $user; Is new registration: $isANewRegistration")
  }

  override fun resetUser() {
    Timber.tag("Analytics").d("Reset User ID")
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    Timber.tag("Analytics").d("Event: $event -> $props")
  }
}
