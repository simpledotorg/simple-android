package org.simple.clinic.analytics

import org.simple.clinic.user.User
import timber.log.Timber

class DebugAnalyticsReporter : AnalyticsReporter {

  override fun setLoggedInUser(user: User) {
    Timber.tag("Analytics").d("User: $user")
  }

  override fun resetUser() {
    Timber.tag("Analytics").d("Reset User ID")
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    Timber.tag("Analytics").d("Event: $event -> $props")
  }
}
