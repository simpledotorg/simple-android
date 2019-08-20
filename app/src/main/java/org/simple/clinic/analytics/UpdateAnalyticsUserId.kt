package org.simple.clinic.analytics

import android.annotation.SuppressLint
import io.reactivex.Scheduler
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import javax.inject.Inject

class UpdateAnalyticsUserId @Inject constructor(private val userSession: UserSession) {

  private val statesToSetUserIdFor = setOf(
      LOGGED_IN, RESETTING_PIN, RESET_PIN_REQUESTED
  )

  @SuppressLint("CheckResult")
  fun listen(scheduler: Scheduler) {
    userSession
        .loggedInUser()
        .filterAndUnwrapJust()
        .filter { it.loggedInStatus in statesToSetUserIdFor }
        .doOnNext(Analytics::setUser)
        .subscribeOn(scheduler)
        .subscribe()
  }
}
