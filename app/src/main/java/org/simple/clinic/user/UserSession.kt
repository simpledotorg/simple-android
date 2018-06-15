package org.simple.clinic.user

import io.reactivex.Observable
import org.simple.clinic.di.AppScope
import java.util.UUID
import javax.inject.Inject

@AppScope
class UserSession @Inject constructor() {

  fun loggedInUser(): Observable<LoggedInUser> {
    return Observable.just(DUMMY_USER)
  }

  companion object {
    private val DUMMY_USER = LoggedInUser(uuid = UUID.randomUUID())
  }
}
