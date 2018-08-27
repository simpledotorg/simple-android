package org.simple.clinic.activity

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.ReportAnalyticsEvents
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.user.UserSession
import org.simple.clinic.widgets.TheActivityLifecycle
import org.simple.clinic.widgets.UiEvent
import org.threeten.bp.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = TheActivity
typealias UiChange = (Ui) -> Unit

class TheActivityController @Inject constructor(
    private val userSession: UserSession,
    private val appLockConfig: Single<AppLockConfig>,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.compose(ReportAnalyticsEvents()).replay().refCount()

    return Observable.mergeArray(
        showAppLock(replayedEvents),
        updateLockTime(replayedEvents))
  }

  private fun showAppLock(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedCanShowAppLock = events
        .ofType<TheActivityLifecycle.Started>()
        .filter { userSession.isUserLoggedIn() }
        .map { Instant.now() > lockAfterTimestamp.get() }
        .replay()
        .refCount()

    val showAppLock = replayedCanShowAppLock
        .filter { show -> show }
        .map { { ui: Ui -> ui.showAppLockScreen() } }

    val unsetLockTime = replayedCanShowAppLock
        .filter { show -> !show }
        .flatMap {
          lockAfterTimestamp.delete()
          Observable.empty<UiChange>()
        }

    return unsetLockTime.mergeWith(showAppLock)
  }

  private fun updateLockTime(events: Observable<UiEvent>): Observable<UiChange> {
    return events
        .ofType<TheActivityLifecycle.Stopped>()
        .filter { userSession.isUserLoggedIn() }
        .filter { !lockAfterTimestamp.isSet }
        .flatMap {
          appLockConfig
              .flatMapObservable {
                lockAfterTimestamp.set(Instant.now().plusMillis(it.lockAfterTimeMillis))
                Observable.empty<UiChange>()
              }
        }
  }
}
