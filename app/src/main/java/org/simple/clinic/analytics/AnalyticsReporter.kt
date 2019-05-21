package org.simple.clinic.analytics

import timber.log.Timber

interface AnalyticsReporter {

  fun setUserIdentity(id: String)

  fun resetUserIdentity()

  fun createEvent(event: String, props: Map<String, Any>)

  /**
   * Safely report events so that the app does not crash if any of the
   * reporters fail.
   **/
  fun safely(message: String = "", block: AnalyticsReporter.() -> Unit) {
    try {
      block()
    } catch (e: Exception) {
      Timber.e(e, if (message.isBlank()) "Could not report event!" else message)
    }
  }
}
