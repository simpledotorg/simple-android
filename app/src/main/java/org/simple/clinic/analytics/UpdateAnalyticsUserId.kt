package org.simple.clinic.analytics

import android.annotation.SuppressLint
import io.reactivex.Completable
import io.reactivex.Scheduler
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.RESETTING_PIN
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.UNAUTHORIZED
import org.simple.clinic.user.UserSession
import org.simple.clinic.util.filterAndUnwrapJust
import javax.inject.Inject

class UpdateAnalyticsUserId @Inject constructor(private val userSession: UserSession) {

  private val statesToSetUserIdFor = setOf(
      LOGGED_IN, RESETTING_PIN, RESET_PIN_REQUESTED
  )

  @SuppressLint("CheckResult")
  fun listen(scheduler: Scheduler) {
    val loggedInUserStream = userSession
        .loggedInUser()
        .filterAndUnwrapJust()
        .replay()
        .refCount()

    val setAnalyticsUserId = loggedInUserStream
        .filter { it.loggedInStatus in statesToSetUserIdFor }
        .flatMapCompletable { user ->
          Completable.fromAction { Analytics.setUser(user) }
        }

    val clearAnalyticsUserId = loggedInUserStream
        .filter { it.loggedInStatus == UNAUTHORIZED }
        .flatMapCompletable {
          Completable.fromAction { Analytics.clearUser() }
        }


    setAnalyticsUserId.mergeWith(clearAnalyticsUserId)
        .subscribeOn(scheduler)
        .subscribe()
  }
}
