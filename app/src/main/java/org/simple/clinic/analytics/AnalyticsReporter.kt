package org.simple.clinic.analytics

import org.simple.clinic.user.User
import timber.log.Timber

interface AnalyticsReporter {

  fun setLoggedInUser(user: User, isANewRegistration: Boolean)

  fun resetUser()

  fun createEvent(event: String, props: Map<String, Any>)
}
