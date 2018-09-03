package org.simple.clinic.analytics

import io.reactivex.Scheduler
import org.simple.clinic.user.User
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import timber.log.Timber
import javax.inject.Inject

class UpdateAnalyticsUserId @Inject constructor(private val userSession: UserSession) {

  fun update(scheduler: Scheduler) {
    userSession.loggedInUser()
        .subscribeOn(scheduler)
        .filter { it is Just<User> }
        .map { (user) -> user!! }
        .filter { it.loggedInStatus == User.LoggedInStatus.LOGGED_IN }
        .subscribe({
          Analytics.setUserId(it.uuid)
        }, {
          Timber.e(it, "Could not update user ID in analytics")
        })
  }
}
