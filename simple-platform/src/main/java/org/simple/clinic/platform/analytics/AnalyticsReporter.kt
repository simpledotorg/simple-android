package org.simple.clinic.platform.analytics

interface AnalyticsReporter {

  fun setLoggedInUser(user: AnalyticsUser, isANewRegistration: Boolean)

  fun resetUser()

  fun createEvent(event: String, props: Map<String, Any>)
}
