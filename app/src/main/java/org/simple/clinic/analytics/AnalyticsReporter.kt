package org.simple.clinic.analytics

import org.simple.clinic.platform.analytics.AnalyticsUser

interface AnalyticsReporter {

  fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean)

  fun resetUser()

  fun createEvent(event: String, props: Map<String, Any>)
}
