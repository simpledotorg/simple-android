package org.simple.clinic.analytics

import org.simple.clinic.user.User
import timber.log.Timber

/**
 * A decorator [AnalyticsReporter] that swallows errors thrown by the
 * wrapped [AnalyticsReporter] so that the app does not crash when
 * there's an error reporting analytics events.
 **/
class ErrorSwallowingReporter(private val reporter: AnalyticsReporter) : AnalyticsReporter {

  override fun setLoggedInUser(user: User, isANewRegistration: Boolean) {
    val errorMessage = if (isANewRegistration) {
      "Error setting newly registered user!"
    } else {
      "Error setting logged in user!"
    }

    swallowErrorsAndReport(errorMessage) {
      reporter.setLoggedInUser(user, isANewRegistration)
    }
  }

  override fun resetUser() {
    swallowErrorsAndReport("Error clearing user!") {
      reporter.resetUser()
    }
  }

  override fun createEvent(event: String, props: Map<String, Any>) {
    swallowErrorsAndReport("Error reporting event!") {
      reporter.createEvent(event, props)
    }
  }

  private inline fun swallowErrorsAndReport(message: String, block: () -> Unit) {
    try {
      block.invoke()
    } catch (e: Throwable) {
      Timber.e(e, if (message.isBlank()) "Could not report event!" else message)
    }
  }
}

fun AnalyticsReporter.swallowErrors(): AnalyticsReporter {
  return ErrorSwallowingReporter(this)
}
