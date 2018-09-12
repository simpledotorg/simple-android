package org.simple.clinic.user

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import org.simple.clinic.util.Just
import org.simple.clinic.util.Optional

class NewlyVerifiedUser : ObservableTransformer<Optional<User>, User> {

  override fun apply(upstream: Observable<Optional<User>>): ObservableSource<User> {
    return upstream
        .filter { it is Just<User> }
        .map { (user) -> UserAndStatus(user!!, user.loggedInStatus) }
        .buffer(2, 1)
        .filter { it.size == 2 }
        .filter { it[0].loggedInStatus == User.LoggedInStatus.OTP_REQUESTED && it[1].loggedInStatus == User.LoggedInStatus.LOGGED_IN }
        .map { it[1].user }
  }

  private data class UserAndStatus(val user: User, val loggedInStatus: User.LoggedInStatus)
}
