package org.simple.clinic.main

import com.f2prateek.rx.preferences2.Preference
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.ObservableTransformer
import io.reactivex.rxkotlin.ofType
import org.simple.clinic.activity.ActivityLifecycle.Started
import org.simple.clinic.activity.ActivityLifecycle.Stopped
import org.simple.clinic.login.applock.AppLockConfig
import org.simple.clinic.patient.PatientRepository
import org.simple.clinic.user.NewlyVerifiedUser
import org.simple.clinic.user.User.LoggedInStatus.LOGGED_IN
import org.simple.clinic.user.User.LoggedInStatus.OTP_REQUESTED
import org.simple.clinic.user.User.LoggedInStatus.RESET_PIN_REQUESTED
import org.simple.clinic.user.UserSession
import org.simple.clinic.user.UserStatus
import org.simple.clinic.util.UtcClock
import org.simple.clinic.util.filterAndUnwrapJust
import org.simple.clinic.util.filterTrue
import org.simple.clinic.widgets.UiEvent
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

typealias Ui = TheActivityUi
typealias UiChange = (Ui) -> Unit

class TheActivityController @Inject constructor(
    private val userSession: UserSession,
    private val appLockConfig: AppLockConfig,
    private val patientRepository: PatientRepository,
    private val utcClock: UtcClock,
    @Named("should_lock_after") private val lockAfterTimestamp: Preference<Instant>
) : ObservableTransformer<UiEvent, UiChange> {

  private val showAppLockForUserStates = setOf(OTP_REQUESTED, LOGGED_IN, RESET_PIN_REQUESTED)

  override fun apply(events: Observable<UiEvent>): ObservableSource<UiChange> {
    val replayedEvents = events.replay().refCount()

    return Observable.mergeArray(
        showAppLock(replayedEvents),
        updateLockTime(replayedEvents),
        displayUserLoggedOutOnOtherDevice(replayedEvents),
        redirectToLoginScreen(),
        redirectToAccessDeniedScreen()
    )
  }

  private fun showAppLock(events: Observable<UiEvent>): Observable<UiChange> {
    val replayedCanShowAppLock = events
        .ofType<Started>()
        .flatMap {
          userSession.loggedInUser()
              .filterAndUnwrapJust()
              .filter { it.status != UserStatus.DisapprovedForSyncing }
              .map { user -> user.loggedInStatus }
              .take(1)
        }
        .filter { it in showAppLockForUserStates }
        .map { Instant.now(utcClock) > lockAfterTimestamp.get() }
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
        .ofType<Stopped>()
        .filter { userSession.isUserLoggedIn() }
        .filter { !lockAfterTimestamp.isSet }
        .doOnNext { lockAfterTimestamp.set(Instant.now(utcClock).plusMillis(appLockConfig.lockAfterTimeMillis)) }
        .flatMap { Observable.empty<UiChange>() }
  }

  private fun displayUserLoggedOutOnOtherDevice(events: Observable<UiEvent>): Observable<UiChange> {
    return events.ofType<Started>()
        .flatMap { userSession.loggedInUser() }
        .compose(NewlyVerifiedUser())
        .map { { ui: Ui -> ui.showUserLoggedOutOnOtherDeviceAlert() } }
  }

  private fun redirectToLoginScreen(): Observable<UiChange> {
    return userSession
        .isUserUnauthorized()
        .distinctUntilChanged()
        .filter { isUserUnauthorized -> isUserUnauthorized }
        .map { Ui::redirectToLogin }
  }

  private fun redirectToAccessDeniedScreen(): Observable<UiChange> {
    return userSession
        .isUserDisapproved()
        .filterTrue()
        .flatMap {
          val fullName = userSession.loggedInUserImmediate()?.fullName
          patientRepository.clearPatientData()
              .andThen(Observable.just { ui: Ui -> ui.showAccessDeniedScreen(fullName!!) })
        }
  }
}
