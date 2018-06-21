package org.simple.clinic.user

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.simple.clinic.di.AppScope
import org.simple.clinic.login.LoginResult
import java.util.UUID
import javax.inject.Inject

@AppScope
class UserSession @Inject constructor() {

  private lateinit var ongoingLoginEntry: OngoingLoginEntry

  fun loggedInUser(): Observable<LoggedInUser> {
    return Observable.just(DUMMY_USER)
  }

  fun saveOngoingLoginEntry(entry: OngoingLoginEntry): Completable {
    return Completable.fromAction {
      this.ongoingLoginEntry = entry
    }
  }

  fun ongoingLoginEntry(): Single<OngoingLoginEntry> {
    return Single.fromCallable { ongoingLoginEntry }
  }

  fun login(): Single<LoginResult> {
    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
  }

  companion object {
    private val DUMMY_USER = LoggedInUser(uuid = UUID.randomUUID())
  }
}
