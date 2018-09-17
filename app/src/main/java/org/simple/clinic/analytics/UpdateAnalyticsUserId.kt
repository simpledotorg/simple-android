package org.simple.clinic.analytics

import io.reactivex.Scheduler
import org.simple.clinic.user.User
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.Just
import timber.log.Timber
import javax.inject.Inject

class UpdateAnalyticsUserId @Inject constructor(private val userSession: UserSession) {

  private val statesToSetUserIdFor = setOf(
      LOGGED_IN, RESETTING_PIN, RESET_PIN_REQUESTED
  )

  fun listen(scheduler: Scheduler) {
    userSession.loggedInUser()
        .subscribeOn(scheduler)
        .filter { it is Just<User> }
        .map { (user) -> user!! }
        .filter { it.loggedInStatus in statesToSetUserIdFor }
        .subscribe({
          Analytics.setUserId(it.uuid)
        }, {
          Timber.e(it, "Could not update user ID in analytics")
        })
  }
}
