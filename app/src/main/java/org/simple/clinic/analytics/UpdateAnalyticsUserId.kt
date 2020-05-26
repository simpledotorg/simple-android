package org.simple.clinic.analytics

import android.annotation.SuppressLint
import org.simple.clinic.platform.analytics.Analytics
import org.simple.clinic.platform.analytics.AnalyticsUser
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.scheduler.SchedulersProvider
import javax.inject.Inject

class UpdateAnalyticsUserId @Inject constructor(
    private val userSession: UserSession,
    private val schedulersProvider: SchedulersProvider
) {

  private val statesToSetUserIdFor = setOf(
      LOGGED_IN, RESETTING_PIN, RESET_PIN_REQUESTED
  )

  @SuppressLint("CheckResult")
  fun listen() {
    userSession
        .loggedInUser()
        .take(1)
        .filterAndUnwrapJust()
        .filter { it.loggedInStatus in statesToSetUserIdFor }
        .map { AnalyticsUser(it.uuid, it.fullName) }
        .doOnNext(Analytics::setLoggedInUser)
        .subscribeOn(schedulersProvider.io())
        .subscribe()
  }
}
